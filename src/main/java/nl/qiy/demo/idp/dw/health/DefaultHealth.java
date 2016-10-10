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

package nl.qiy.demo.idp.dw.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;

public class DefaultHealth extends HealthCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHealth.class);
    /**
     * The version from the jar's manifest. Will not change during the lifetime
     * of the app, so it can be final
     */
    private final String version;

    public DefaultHealth() {
        super();
        version = this.getClass().getPackage().getImplementationVersion();
    }

    @Override
    protected Result check() throws Exception {
        if (version != null) {
            return Result.healthy("Status: OK Timestamp: " + System.currentTimeMillis() + " Version: " + version);
        }
        LOGGER.error("No version found in the jar's manifest");
        return Result.unhealthy("No version found");
    }
}
