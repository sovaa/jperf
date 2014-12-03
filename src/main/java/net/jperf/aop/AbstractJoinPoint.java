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

/**
 * AOP-framework agnostic join point.
 *
 * An AOP implementation agnostic interface which offers all information required to do a measure, proceed original
 * method and log result in customizable way. Specific Join Point implementations in AOP libraries/frameworks
 * should implement it wrapping their own internal structures.
 *
 * @author Marcin Zajączkowski, 2010-01-14
 *
 * @since 0.9.13
 */
public interface AbstractJoinPoint {

    /**
     * Calls profiled method and returns its result.
     *
     * @return result of proceeding
     * @throws Throwable thrown exception
     */
    public Object proceed() throws Throwable;

    /**
     * Returns an object whose method was annotated (profiled).
     *
     * @return an object whose method was annotated
     */
    public Object getExecutingObject();

    /**
     * Returns a parameters (arguments) array of processing method.
     *
     * @return array of parameters
     */
    public Object[] getParameters();

    /**
     * Returns a processing method name.
     *
     * @return processing method name
     */
    public String getMethodName();

    /**
     * Returns the declaring class of the method that was annotated.
     *
     * @return the declaring class of the method that was annotated
     */
    public Class<?> getDeclaringClass();
}
