/* Copyright (c) 2011 Brett Randall.
 * All rights reserved.  http://www.perf4j.org
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
package net.jperf.helpers;

import java.util.Properties;

import junit.framework.TestCase;

public class PackageParentPropertiesTest extends TestCase {

    // this actually loads a PackageParentProperties singleton
    final Properties properties = Perf4jProperties.INSTANCE;

    public void testFullPath() {
        assertEquals("some tag", properties.get("tag." + getClass().getName()));
        assertEquals("some message", properties.get("message." + getClass().getName()));
    }

    public void testFullPathGetProperty() {
        assertEquals("some tag", properties.getProperty("tag." + getClass().getName()));
        assertEquals("some message", properties.getProperty("message." + getClass().getName()));
    }

    public void testParentPath() {
        assertEquals("parent tag", properties.get("tag.net.jperf.helpers.ClassNotInProperties"));
        assertEquals("parent message", properties.get("message.net.jperf.helpers.ClassNotInProperties"));
    }

    public void testParentPathGetProperty() {
        assertEquals("parent tag", properties.getProperty("tag.net.jperf.helpers.ClassNotInProperties"));
        assertEquals("parent message", properties.getProperty("message.net.jperf.helpers.ClassNotInProperties"));
    }

    public void testDefaultPath() {
        assertEquals("{$methodName}", properties.get("tag.no.common.path.MyClass"));
        assertEquals("", properties.get("message.no.common.path.MyClass"));
    }

    public void testDefaultPathGetProperty() {
        assertEquals("{$methodName}", properties.getProperty("tag.no.common.path.MyClass"));
        assertEquals("", properties.getProperty("message.no.common.path.MyClass"));
    }

    public void testNoDefaults() {
        assertNull(properties.getProperty("nomatch"));
    }

    public void testDefaultProperty() {
        assertEquals("default", properties.getProperty("nomatch", "default"));
    }
}
