/*
 * This work is protected under copyright law in the Kingdom of
 * The Netherlands. The rules of the Berne Convention for the
 * Protection of Literary and Artistic Works apply.
 * Digital Me B.V. is the copyright owner.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.qiy.demo.idp.dw.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: friso should have written a comment here to tell us what this class does
 *
 * @author friso
 * @since 6 jan. 2017
 */
@Path("test")
public class TestInvokerResource {
    /** 
     * Standard SLF4J Logger 
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestInvokerResource.class);
    private String testPage;
    private String responsePage;

    @Path("create")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getInvoker() throws IOException, URISyntaxException {
        if (testPage == null) {
            testPage = new String(
                    Files.readAllBytes(Paths.get(TestInvokerResource.class.getResource("/test-call.html").toURI())));
        }
        return testPage;
    }
    
    @Path("redirect")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String redirect() throws IOException, URISyntaxException {
        if (responsePage == null) {
            responsePage = new String(
                    Files.readAllBytes(Paths.get(TestInvokerResource.class.getResource("/test-respond.html").toURI())));
        }
        return responsePage;
    }

    @Path("redirect")
    @POST
    @Produces(MediaType.TEXT_HTML)
    public static String redirectPost(@Context HttpServletRequest request) {
        return request.getParameterMap().toString();
    }

}
