package org.perf4j.aop;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.context.HashMapContext;
import org.perf4j.LoggingStopWatch;
import org.perf4j.helpers.Perf4jProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This AgnosticTimingAspect class contains all the logic for executing a profiled method with appropriate timing calls,
 * but in an AOP-framework-agnostic way. You may choose to either extend or wrap this class to create an aspect in
 * your desired framework. For example, if you look at the {@link org.perf4j.aop.AbstractTimingAspect}, you can see
 * that it delegates all functionality to this class - it just includes the necessary AspectJ annotations and wraps
 * the AspectJ-specific ProceedingJoinPoint as an {@link org.perf4j.aop.AbstractJoinPoint}.
 *
 * @author Marcin Zajączkowski, Alex Devine 
 */
public class AgnosticTimingAspect {
    /**
     * This Map is used to cache compiled JEXL expressions. While theoretically unbounded, in reality the number of
     * possible keys is equivalent to the number of unique JEXL expressions created in @Profiled annotations, which
     * will have to be loaded in memory anyway when the class is loaded.
     */
    private Map<String, Expression> jexlExpressionCache = new ConcurrentHashMap<String, Expression>(64, .75F, 16);

    /**
     * This method actually executes the profiled method. Your AOP-framework-specific class should delegate to this
     * method to proceed with execution.
     *
     * @param joinPoint The AOP join point - usually this will just be a simple wrapper around the
     *                  AOP-framework-specific join point.
     * @param profiled  The Profiled annotation that was set on the method being profiled.
     * @param stopWatch This LogginStopWatch should be started JUST before this method is called.
     * @return The return value from the profiled method.
     * @throws Throwable Exception thrown by the profiled method will bubble up.
     */
    public Object runProfiledMethod(AbstractJoinPoint joinPoint, Profiled profiled, LoggingStopWatch stopWatch)
            throws Throwable {
        //if we're not going to end up logging the stopwatch, just run the wrapped method
        if (!stopWatch.isLogging()) {
            return joinPoint.proceed();
        }

        stopWatch.setTimeThreshold(profiled.timeThreshold());
        stopWatch.setNormalAndSlowSuffixesEnabled(profiled.normalAndSlowSuffixesEnabled());

        Object retVal = null;
        Throwable exceptionThrown = null;
        try {
            return retVal = joinPoint.proceed();
        } catch (Throwable t) {
            throw exceptionThrown = t;
        } finally {
            String tag = getStopWatchTag(profiled, joinPoint, retVal, exceptionThrown);
            String message = getStopWatchMessage(profiled, joinPoint, retVal, exceptionThrown);

            if (profiled.logFailuresSeparately()) {
                tag = (exceptionThrown == null) ? tag + ".success" : tag + ".failure";
            }

            stopWatch.stop(tag, message);
        }
    }

    /**
     * Helper method gets the tag to use for StopWatch logging. Performs JEXL evaluation if necessary.
     *
     * @param profiled        The profiled annotation that was attached to the method.
     * @param joinPoint       The AbstractJoinPoint encapulates the method around which this aspect advice runs.
     * @param returnValue     The value returned from the execution of the profiled method, or null if the method
     *                        returned void or an exception was thrown.
     * @param exceptionThrown The exception thrown, if any, by the profiled method. Will be null if the method
     *                        completed normally.
     * @return The value to use as the StopWatch tag.
     */
    protected String getStopWatchTag(Profiled profiled, AbstractJoinPoint joinPoint, Object returnValue, Throwable exceptionThrown) {
        String tag;
        if (Profiled.DEFAULT_TAG_NAME.equals(profiled.tag())) {
            // look for properties-based default
            // if the tag name is not explicitly set on the Profiled annotation,
            final String tagString = String.format("tag.%s.%s",
                    joinPoint.getDeclaringClass().getName(),
                    joinPoint.getMethodName());

            tag = Perf4jProperties.INSTANCE.getProperty(tagString);
            String defaultTag = Perf4jProperties.INSTANCE.getProperty("defaultTag");

            if (tag == null && defaultTag == null) {
                // fall back to using the name of the method being annotated.
                tag = joinPoint.getMethodName();
            }
            else if (tag != null && tag.contains("{")) {
                tag = evaluateJexl(tag, joinPoint, returnValue, exceptionThrown);
            }
            else if (tag == null) {
                tag = evaluateJexl(defaultTag, joinPoint, returnValue, exceptionThrown);
            }
        } else if (profiled.el() && profiled.tag().contains("{")) {
            tag = evaluateJexl(profiled.tag(), joinPoint, returnValue, exceptionThrown);
        } else {
            tag = profiled.tag();
        }
        return tag;
    }


    /**
     * Helper method get the message to use for StopWatch logging. Performs JEXL evaluation if necessary.
     *
     * @param profiled        The profiled annotation that was attached to the method.
     * @param joinPoint       The AbstractJoinPoint encapulates the method around which this aspect advice runs.
     * @param returnValue     The value returned from the execution of the profiled method, or null if the method
     *                        returned void or an exception was thrown.
     * @param exceptionThrown The exception thrown, if any, by the profiled method. Will be null if the method
     *                        completed normally.
     * @return The value to use as the StopWatch message.
     */
    protected String getStopWatchMessage(Profiled profiled,
                                         AbstractJoinPoint joinPoint,
                                         Object returnValue,
                                         Throwable exceptionThrown) {
        String message;
        if (profiled.message().length() == 0) {
            // look for properties-based default
            // if the message name is not explicitly set on the Profiled annotation,
            final StringBuilder sb = new StringBuilder("message.").append(joinPoint.getDeclaringClass().getName())
                    .append('.').append(joinPoint.getMethodName());
            message = Perf4jProperties.INSTANCE.getProperty(sb.toString());

            if (message == null) {
                // may be null, that's OK
                return message;
            } else {
                if (message.indexOf("{") >= 0) {
                    message = evaluateJexl(message,
                            joinPoint,
                            returnValue,
                            exceptionThrown);
                }
            }
        } else if (profiled.el() && profiled.message().indexOf("{") >= 0) {
            message = evaluateJexl(profiled.message(),
                                   joinPoint,
                                   returnValue,
                                   exceptionThrown);
            if ("".equals(message)) {
                message = null;
            }
        } else {
            message = profiled.message();
        }
        return message;
    }

    /**
     * Helper method is used to parse out {expressionLanguage} elements from the text and evaluate the strings using
     * JEXL.
     *
     * @param text            The text to be parsed.
     * @param joinPoint       The AbstractJoinPoint encapulates the method around which this aspect advice runs.
     * @param returnValue     The value returned from the execution of the profiled method, or null if the method
     *                        returned void or an exception was thrown.
     * @param exceptionThrown The exception thrown, if any, by the profiled method. Will be null if the method
     *                        completed normally.
     * @return The evaluated string.
     * @see Profiled#el()
     */
	protected String evaluateJexl(String text, AbstractJoinPoint joinPoint, Object returnValue, Throwable exceptionThrown) {
        StringBuilder retVal = new StringBuilder(text.length());
        JexlContext jexlContext = populateJexlContext(joinPoint, returnValue, exceptionThrown);

        // look for {expression} in the passed in text
        int bracketIndex;
        int lastCloseBracketIndex = -1;

        while ((bracketIndex = text.indexOf('{', lastCloseBracketIndex + 1)) >= 0) {
            appendNonJexlText(retVal, text, bracketIndex, lastCloseBracketIndex);
            lastCloseBracketIndex = lastCloseBracketIndex(text, bracketIndex);
            String expressionText = textBetweenBrackets(text, bracketIndex, lastCloseBracketIndex);
            evaluateJexlAndSetOnResult(retVal, expressionText, jexlContext);
        }

        appendFinalPart(retVal, text, lastCloseBracketIndex);
        return retVal.toString();
    }

    private void appendFinalPart(StringBuilder retVal, String text, int lastCloseBracketIndex) {
        if (lastCloseBracketIndex < text.length()) {
            retVal.append(text.substring(lastCloseBracketIndex + 1, text.length()));
        }
    }

    private void evaluateJexlAndSetOnResult(StringBuilder retVal, String expressionText, JexlContext jexlContext) {
        if (expressionText.length() == 0) {
            return;
        }
        try {
            Object result = getJexlExpression(expressionText).evaluate(jexlContext);
            retVal.append(result);
        } catch (Exception e) {
            //we don't want to propagate exceptions up
            retVal.append("_EL_ERROR_");
        }
    }

    private String textBetweenBrackets(String text, int bracketIndex, int lastCloseBracketIndex) {
        return text.substring(bracketIndex + 1, lastCloseBracketIndex);
    }

    private void appendNonJexlText(StringBuilder retVal, String text, int bracketIndex, int lastCloseBracketIndex) {
        retVal.append(text.substring(lastCloseBracketIndex + 1, bracketIndex));
    }

    private int lastCloseBracketIndex(String text, int bracketIndex) {
        int index = text.indexOf('}', bracketIndex + 1);
        if (index == -1) {
            //if there wasn't a closing bracket index just go to the end of the string
            index = text.length();
        }

        return index;
    }

    @SuppressWarnings("unchecked")
    private JexlContext populateJexlContext(AbstractJoinPoint joinPoint, Object returnValue, Throwable exceptionThrown) {
        Object[] args = joinPoint.getParameters();

        //create a JexlContext to be used in all evaluations
        JexlContext jexlContext = new HashMapContext();
        for (int i = 0; i < args.length; i++) {
            jexlContext.getVars().put("$" + i, args[i]);
        }

        jexlContext.getVars().put("$methodName", joinPoint.getMethodName());
        jexlContext.getVars().put("$this", joinPoint.getExecutingObject());
        jexlContext.getVars().put("$class", joinPoint.getDeclaringClass().getName());
        jexlContext.getVars().put("$classSimpleName", joinPoint.getDeclaringClass().getSimpleName());
        jexlContext.getVars().put("$return", returnValue);
        jexlContext.getVars().put("$exception", exceptionThrown);

        return jexlContext;
    }

    /**
     * Helper method gets a compiled JEXL expression for the specified expression text, either from the cache or by
     * creating a new compiled expression.
     *
     * @param expressionText The JEXL expression text
     * @return A compiled JEXL expression representing the expression text
     * @throws Exception Thrown if there was an error compiling the expression text
     */
    protected Expression getJexlExpression(String expressionText) throws Exception {
        Expression retVal = jexlExpressionCache.get(expressionText);
        if (retVal == null) {
            //Don't need synchronization here - if we end up calling createExpression in 2 separate threads, that's fine
            jexlExpressionCache.put(expressionText, retVal = ExpressionFactory.createExpression(expressionText));
        }
        return retVal;
    }
}
