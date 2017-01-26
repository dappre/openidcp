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

package nl.qiy.oic.op.dw;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.util.Duration;
import nl.qiy.oic.op.qiy.QRConfig;
import nl.qiy.oic.op.qiy.QiyNodeConfig;
import nl.qiy.openid.op.spi.impl.config.CryptoConfig;
import nl.qiy.openid.op.spi.impl.config.JWKConfig;
import nl.qiy.openid.op.spi.impl.config.OAuthClientConfig;
import nl.qiy.openid.op.spi.impl.config.OpSdkSpiImplConfiguration;
import nl.qiy.openid.op.spi.impl.jedis.JedisConfiguration;

public class QiyOICConfiguration extends OpSdkSpiImplConfiguration {

    private boolean jerseyConfigDone = false;

    @Valid
    @NotNull
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

    // @formatter:off
    @JsonCreator //NOSONAR
    public QiyOICConfiguration(@JsonProperty("qrConfig") QRConfig qrConfig, // NOSONAR
            @JsonProperty("sessionTimeoutInSeconds") Integer sessionTimeoutInSeconds,
            @JsonProperty("clientConfig") List<OAuthClientConfig> clientConfig,
            @JsonProperty("nodeConfig") QiyNodeConfig nodeConfig,
            @JsonProperty("cryptoConfig") CryptoConfig cryptoConfig, @JsonProperty("baseUri") String baseUri,
            @JsonProperty("registerCallbackUri") String registerCallbackUri, @JsonProperty("iss") String iss,
            @JsonProperty("jwkConfigs") Map<String, Map<String, JWKConfig>> jwkConfigs,
            @JsonProperty("cardMsgUri") String cardMsgUri,
            @JsonProperty("requireCard") String cardLoginOption, 
            @JsonProperty("welcomeMessage") String welcomeMessage,
            @JsonProperty("jedisConfiguration") JedisConfiguration jedisConfiguration,
            @JsonProperty("jerseyClient") JerseyClientConfiguration jerseyClient) { // @formatter:on 
        super(qrConfig, sessionTimeoutInSeconds, clientConfig, nodeConfig, cryptoConfig, baseUri, registerCallbackUri,
                iss, jwkConfigs, cardMsgUri, cardLoginOption, welcomeMessage, jedisConfiguration);
        if (jerseyClient != null) {
            this.jerseyClient = jerseyClient;
        }
    }

    @JsonProperty("jerseyClient")
    public void setJerseyClientConfiguration(JerseyClientConfiguration jerseyClient) {
        this.jerseyClient = jerseyClient;
    }

    @JsonProperty("jerseyClient")
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        if (!jerseyConfigDone) {
            if (jerseyClient != null) {
                if (jerseyClient.getConnectionTimeout().equals(Duration.milliseconds(500))) {
                    jerseyClient.setConnectionTimeout(Duration.seconds(10));
                }
                if (jerseyClient.getConnectionRequestTimeout().equals(Duration.milliseconds(500))) {
                    jerseyClient.setConnectionRequestTimeout(Duration.seconds(10));
                }
                if (jerseyClient.getTimeout().equals(Duration.milliseconds(500))) {
                    jerseyClient.setTimeout(Duration.seconds(10));
                }
            }
            jerseyConfigDone = true;
        }
        return jerseyClient;
    }
}
