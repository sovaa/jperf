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
package net.jperf.log4j.servlet;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests the log4j GraphingServlet.
 */
public class Log4JGraphingServletTest extends TestCase {

    /**
     * Test for http://jira.codehaus.org/browse/PERFFORJ-28
     *
     * @throws Exception Thrown on error
     */
    public void testUnknownGraphName() throws Exception {
        GraphingServlet servlet = new GraphingServlet();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/jperf");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addParameter("graphName", "unknownGraph");

        servlet.service(request, response);

        //we should get a message that the graph name was unknown, not an NPE
        String content = response.getContentAsString();
        assertTrue("Didn't find expected warning message in response: " + content,
                   content.indexOf("Unknown graph name: unknownGraph") >= 0);
    }
}
