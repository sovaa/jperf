package org.jperf.slf4j.aop;

import org.jperf.aop.AbstractEjbTimingAspect;
import org.jperf.slf4j.Slf4JStopWatch;
import org.slf4j.LoggerFactory;

/**
 * This TimingAspect implementation uses a SLF4J Logger instance to persist StopWatch log messages.
 * To use this interceptor in your code, you should add this class name to the {@link javax.interceptor.Interceptors}
 * annotation on the EJB to be profiled.
 *
 * @author Alex Devine
 */
public class EjbTimingAspect extends AbstractEjbTimingAspect {
    protected Slf4JStopWatch newStopWatch(String loggerName, String levelName) {
        int levelInt = Slf4JStopWatch.mapLevelName(levelName);
        return new Slf4JStopWatch(LoggerFactory.getLogger(loggerName), levelInt, levelInt);
    }
}
