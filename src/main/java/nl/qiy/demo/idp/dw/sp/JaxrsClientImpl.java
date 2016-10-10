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

package nl.qiy.demo.idp.dw.sp;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.ws.rs.client.Client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.qiy.oic.op.service.spi.JaxrsClient;

public class JaxrsClientImpl implements JaxrsClient {
    /**
     * Standard SLF4J Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JaxrsClientImpl.class);

    private static Client client;

    private ValidatorFactory validationFactory = Validation.buildDefaultValidatorFactory();

    @Override
    public boolean isHealthy() {
        boolean result = JaxrsClientImpl.client != null
                && validationFactory.getValidator().validate(JaxrsClientImpl.client).isEmpty();
        LOGGER.debug("{} init called: {}", this.getClass(), result);
        return result;
    }

    @Override
    public Client getClient() {
        return JaxrsClientImpl.client;
    }

    public static void setClient(Client client) {
        JaxrsClientImpl.client = client;
    }

}
