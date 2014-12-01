/*
 * This software is the confidential and proprietary information of
 * Sigma Systems Innovation. ("Confidential Information"). You shall
 * not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sigma Systems Innovation.
 *
 * COPYRIGHT (C) 2014 SIGMA SYSTEMS INNOVATION AB.
 * All rights reserved.
 */
package org.perf4j.aop;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:oscar.eriks@gmail.com">Oscar Eriksson</a>
 * @date 12/1/14
 */
public class AgnosticTimingAspectTest {
    private AgnosticTimingAspect agnosticTimingAspect;
    private AbstractJoinPoint joinPoint;
    private final String methodName = "testMethodName";
    private final String returnValue = "returnString";
    private final String className = AgnosticTimingAspectTest.class.getName();
    private final String classSimpleName = AgnosticTimingAspectTest.class.getSimpleName();
    private final Throwable throwable = new Throwable();

    @Before
    public void setup() {
        agnosticTimingAspect = new AgnosticTimingAspect();
        joinPoint = new AbstractJoinPoint() {
            public Object proceed() throws Throwable {
                return null;
            }

            public Object getExecutingObject() {
                return new Object();
            }

            public Object[] getParameters() {
                return new Object[0];
            }

            public String getMethodName() {
                return methodName;
            }

            public Class<?> getDeclaringClass() {
                return AgnosticTimingAspectTest.class;
            }
        };
    }

    @Test
    public void testClassSimpleNameJexlExpansion() {
        String text = "{$classSimpleName}";
        String evaluated  = evaluate(text);
        assertEquals(classSimpleName, evaluated);
    }

    @Test
    public void testClassJexlExpansion() {
        String text = "{$class}";
        String evaluated  = evaluate(text);
        assertEquals(className, evaluated);
    }

    @Test
    public void testMethodNameJexlExpansion() {
        String text = "{$methodName}";
        String evaluated  = evaluate(text);
        assertEquals(methodName, evaluated);
    }

    @Test
    public void testSimpleClassNameAndMethodJexlExpansion() {
        String text = "{$classSimpleName}#{$methodName}";
        String evaluated  = evaluate(text);
        assertEquals(classSimpleName + "#" + methodName, evaluated);
    }

    @Test
    public void testReturnValueJexlExpansion() {
        String text = "{$return}";
        String evaluated  = evaluate(text);
        assertEquals(returnValue, evaluated);
    }

    @Test
    public void testExceptionJexlExpansion() {
        String text = "{$exception}";
        String evaluated  = evaluate(text);
        assertEquals(throwable.getClass().getName(), evaluated);
    }

    @Test
    public void testEvaluateJexlMissingFirstBracketIsNotRecognized() {
        String text = "$methodName}";
        String evaluated  = evaluate(text);
        assertEquals("$methodName}", evaluated);
    }

    @Test
    public void testEvaluateJexlMissingLastBracketIsValid() {
        String text = "{$methodName";
        String evaluated = evaluate(text);
        assertEquals(methodName, evaluated);
    }

    @Test
    public void testEvaluateJexlSpaceBeforeEndBracket() {
        String text = "{$methodName }";
        String evaluated = evaluate(text);
        assertEquals(methodName, evaluated);
    }

    @Test
    public void testEvaluateJexlSpaceAfterStartBracket() {
        String text = "{ $methodName}";
        String evaluated = evaluate(text);
        assertEquals(methodName, evaluated);
    }

    @Test
    public void testEvaluateJexlSpaceBeforeAndAfterJexlVariable() {
        String text = "{ $methodName }";
        String evaluated = evaluate(text);
        assertEquals(methodName, evaluated);
    }

    @Test
    public void testInvalidJexlResultsInElError() {
        String text = "{$methodName#$return}";
        String evaluated = evaluate(text);
        assertEquals("_EL_ERROR_", evaluated);
    }
    
    private String evaluate(String text) {
        return agnosticTimingAspect.evaluateJexl(text, joinPoint, returnValue, throwable);
    }
}
