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
import net.jperf.helpers.JperfProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:oscar.eriksson@sigma.se">Oscar Eriksson</a>
 * @date 2/13/15
 */
public class AgnosticTimingAspectThresholdTest {
    private AgnosticTimingAspect agnosticTimingAspect;
    private AbstractJoinPoint joinPoint;
    private TestObject testObject;

    @After
    public void tearDown() {
        InMemoryTimingAspect.logStrings.clear();
    }

    @Before
    public void setup() {
        JperfProperties.INSTANCE.remove(Profiled.THRESHOLD_FIELD_NAME);
        testObject = new TestObject();
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
                return "methodName";
            }

            public Class<?> getDeclaringClass() {
                return AgnosticTimingAspectTest.class;
            }
        };
    }

    @Test
    public void testStopWatchTimeThresholdDefaultValueIsZero() throws Throwable {
        LoggingStopWatch stopWatch = new LoggingStopWatch();
        agnosticTimingAspect.runProfiledMethod(joinPoint, DefaultProfiled.INSTANCE, stopWatch);
        assertEquals(0, stopWatch.getTimeThreshold());
    }

    @Test
    public void testProfiledTimeThresholdDefaultValueIsMinusOne() throws Throwable {
        assertEquals(-1, DefaultProfiled.INSTANCE.timeThreshold());
    }

    @Test
    public void testProfiledAnnotationTimeThresholdDefaultValueIsMinusOne() throws Throwable {
        long defaultValueOnAnnotation = (Long)testObject.getClass().getMethod("annotation").getDeclaredAnnotations()[0]
                .annotationType().getMethod(Profiled.THRESHOLD_FIELD_NAME).getDefaultValue();
        assertEquals(-1, defaultValueOnAnnotation);
    }

    @Test
    public void testThatProfiledAnnotationDefaultTimeThresholdValueIsMinusOne() throws Exception {
        testObject.noTimeThresholdSetOnAnnotation();
        assertNotNull("should have logged since default timeout is 0ms", InMemoryTimingAspect.getLastLoggedString());
    }

    @Test
    public void testDefaultThresholdWillLogMessageWithNoSleepWillLog() throws Exception {
        testObject.noTimeThresholdSetOnAnnotation();
        assertNotNull("should have logged since default timeout is 0ms", InMemoryTimingAspect.getLastLoggedString());
    }

    @Test
    public void testDefaultThresholdWillLogMessageWith100MsSleepWillLog() throws Exception {
        testObject.noTimeThresholdSetOnAnnotationSleep100Ms();
        assertNotNull("should have logged since default timeout is 0ms", InMemoryTimingAspect.getLastLoggedString());
    }

    @Test
    public void test50MsThresholdOnAnnotationWith100MsSleepWillLog() throws Exception {
        testObject.timeThresholdOf50MsSetOnAnnotationSleep100Ms();
        assertNotNull("should have logged since sleep is longer than set threshold", InMemoryTimingAspect.getLastLoggedString());
    }

    @Test
    public void tes100MsThresholdOnAnnotationWithNoSleepWillNotLog() throws Exception {
        testObject.timeThresholdOf100MsSetOnAnnotationNoSleep();
        assertNull("should not have logged since sleep is shorter than set threshold", InMemoryTimingAspect.getLastLoggedString());
    }

    @Test
    public void testConfiguredThresholdSurpessesLogging() {
        JperfProperties.INSTANCE.setProperty(Profiled.THRESHOLD_FIELD_NAME, "2000");
        testObject.noTimeThresholdSetOnAnnotationSleep100Ms();
        assertNull("should not have logged", InMemoryTimingAspect.getLastLoggedString());
    }

    @Test
    public void testConfiguredThresholdDoesNotSurpessesLogging() {
        JperfProperties.INSTANCE.setProperty(Profiled.THRESHOLD_FIELD_NAME, "10");
        testObject.noTimeThresholdSetOnAnnotationSleep100Ms();
        assertNotNull("should have logged", InMemoryTimingAspect.getLastLoggedString());
    }

    @Test
    public void testNoConfiguredThresholdDoesNotSurpessesLogging() {
        testObject.noTimeThresholdSetOnAnnotationSleep100Ms();
        assertNotNull("should have logged", InMemoryTimingAspect.getLastLoggedString());
    }

    private static class TestObject {
        @Profiled
        public void annotation() throws IllegalAccessException {
            throw new IllegalAccessException("should not be executed, only for retrieving annotation");
        }

        @Profiled
        public void noTimeThresholdSetOnAnnotation() {
            System.out.println("noTimeThresholdSetOnAnnotation doing nothing");
        }

        @Profiled
        public void noTimeThresholdSetOnAnnotationSleep100Ms() {
            sleep(100);
        }

        @Profiled(timeThreshold = 50)
        public void timeThresholdOf50MsSetOnAnnotationSleep100Ms() {
            sleep(100);
        }

        @Profiled(timeThreshold = 100)
        public void timeThresholdOf100MsSetOnAnnotationNoSleep() {
            System.out.println("timeThresholdOf100MsSetOnAnnotationNoSleep doing nothing");
        }

        private void sleep(long ms) {
            try {
                Thread.sleep(ms);
            }
            catch (InterruptedException e) {
                // nothing
            }
        }
    }
}
