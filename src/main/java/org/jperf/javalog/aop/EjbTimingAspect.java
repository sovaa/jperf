package org.jperf.javalog.aop;

import org.jperf.aop.AbstractEjbTimingAspect;
import org.jperf.javalog.JavaLogStopWatch;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This TimingAspect implementation uses java.util.logging to persist StopWatch log messages.
 * To use this interceptor in your code, you should add this class name to the {@link javax.interceptor.Interceptors}
 * annotation on the EJB to be profiled.
 *
 * @author Alex Devine
 */
public class EjbTimingAspect extends AbstractEjbTimingAspect {
    protected JavaLogStopWatch newStopWatch(String loggerName, String levelName) {
        Level level = JavaLogStopWatch.mapLevelName(levelName);
        return new JavaLogStopWatch(Logger.getLogger(loggerName), level, level);
    }
}
