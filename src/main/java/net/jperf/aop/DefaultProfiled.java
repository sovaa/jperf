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

import net.jperf.StopWatch;

import java.lang.annotation.Annotation;

/**
 * This unusual concrete implementation of this Profiled annotation interface is used for cases where some
 * interception frameworks may want to profile methods that DON'T have a profiled annotation (for example, EJB 3.0
 * interceptors). See the code for {@link AbstractEjbTimingAspect} for an example of how this is
 * used.
 */
@SuppressWarnings("all")
public class DefaultProfiled implements Profiled {
    public static final DefaultProfiled INSTANCE = new DefaultProfiled();

    private DefaultProfiled() { }

    public String tag() { return DEFAULT_TAG_NAME; }

    public String message() { return ""; }

    public String logger() { return StopWatch.DEFAULT_LOGGER_NAME; }

    public String level() { return "INFO"; }

    public boolean el() { return true; }

    public boolean logFailuresSeparately() { return false; }

    public long timeThreshold() { return -1; }
    
    public boolean normalAndSlowSuffixesEnabled() { return false; }
    
    public Class<? extends Annotation> annotationType() { return getClass(); }
}
