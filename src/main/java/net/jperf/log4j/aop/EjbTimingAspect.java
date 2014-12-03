/*
 * Copyright (c) 2008-2015 JPerf
 * All rights reserved.  http://www.jperf.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
