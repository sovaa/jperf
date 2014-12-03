package net.jperf.commonslog.aop;

import net.jperf.aop.AbstractEjbTimingAspect;
import org.apache.commons.logging.LogFactory;
import net.jperf.commonslog.CommonsLogStopWatch;

/**
 * This EjbTimingAspect implementation uses an Apache Commons Logging Log instance to persist StopWatch log messages.
 * To use this interceptor in your code, you should add this class name to the {@link javax.interceptor.Interceptors}
 * annotation on the EJB to be profiled.
 *
 * @author Alex Devine
 */
public class EjbTimingAspect extends AbstractEjbTimingAspect {
    protected CommonsLogStopWatch newStopWatch(String loggerName, String levelName) {
        int levelInt = CommonsLogStopWatch.mapLevelName(levelName);
        return new CommonsLogStopWatch(LogFactory.getLog(loggerName), levelInt, levelInt);
    }
}
