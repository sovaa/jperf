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

package net.jperf.aop;

import net.jperf.LoggingStopWatch;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is used by the EjbAopTest to check when the aspect was called
 */
public class EjbInMemoryTimingAspect extends AbstractEjbTimingAspect {
    public static List<String> logStrings = Collections.synchronizedList(new ArrayList<String>());

    protected LoggingStopWatch newStopWatch(final String loggerName, final String levelName) {
        return new LoggingStopWatch() {
            private static final long serialVersionUID = -8258832873829050541L;

            public boolean isLogging() {
                return Level.toLevel(levelName).toInt() >= Level.INFO_INT;
            }

            protected void log(String stopWatchAsString, Throwable exception) {
                EjbInMemoryTimingAspect.logStrings.add(stopWatchAsString);
            }
        };
    }

    public static String getLastLoggedString() {
        if (logStrings.size() > 0) {
            return logStrings.get(logStrings.size() - 1);
        } else {
            return null;
        }
    }
}
