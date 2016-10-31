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

package nl.qiy.demo.idp.dw.cli;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.sse.SseFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectWriter;

import io.dropwizard.Application;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.qiy.demo.idp.dw.DemoIdPConfiguration;
import nl.qiy.oic.op.qiy.QiyNodeClient;

/**
 * TODO: friso should have written a comment here to tell us what this class does
 *
 * @author Friso Vrolijken
 * @since 2 jun. 2016
 */
public class ApiInfoCommand extends EnvironmentCommand<DemoIdPConfiguration> {
    /**
     * 
     */
    private static final ObjectWriter PRETTY_MAP_WRITER = Jackson.newObjectMapper().writerWithDefaultPrettyPrinter().forType(HashMap.class);
    /**
     * Standard SLF4J Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiInfoCommand.class);

    public ApiInfoCommand(Application<DemoIdPConfiguration> config) {
        super(config, "apiinfo", "Will query the user node for the API info request");
    }

    @Override
    protected void run(Environment environment, Namespace namespace, DemoIdPConfiguration configuration)
            throws Exception {
        // @formatter:off
        Client client = new JerseyClientBuilder(environment)
                .using(configuration.getJerseyClientConfiguration())
                .build(getName()); // @formatter:on 
        client.register(SseFeature.class);
        // @formatter:off
        Response response = client
                .target(configuration.nodeConfig.endpoint)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, QiyNodeClient.getAuthHeader(null))
                .header("password", configuration.nodeConfig.password)
                .get();
        // @formatter:on
        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            Map<String, Object> map = response.readEntity(HashMap.class);
            String asString = PRETTY_MAP_WRITER.writeValueAsString(map);
            LOGGER.warn("Response\n{}", asString);
        } else {
            LOGGER.error("failed with status code {} ({}) from api request", response.getStatus(),
                    response.getStatusInfo());
        }
    }

}
