package net.jperf.log4j.aop;

import net.jperf.aop.AbstractEjbTimingAspect;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import net.jperf.log4j.Log4JStopWatch;

/**
 * This TimingAspect implementation uses Log4j to persist StopWatch log messages.
 * To use this interceptor in your code, you should add this class name to the {@link javax.interceptor.Interceptors}
 * annotation on the EJB to be profiled.
 *
 * @author Alex Devine
 */
public class EjbTimingAspect extends AbstractEjbTimingAspect {
    protected Log4JStopWatch newStopWatch(String loggerName, String levelName) {
        Level level = Level.toLevel(levelName, Level.INFO);
        return new Log4JStopWatch(Logger.getLogger(loggerName), level, level);
    }
}
