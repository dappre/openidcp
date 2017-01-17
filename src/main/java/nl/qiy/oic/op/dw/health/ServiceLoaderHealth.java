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

package nl.qiy.oic.op.dw.health;

import com.codahale.metrics.health.HealthCheck;

import nl.qiy.oic.op.ContextListener;

/**
 * Checks to see if all service loader classes are happy
 *
 * @author Friso Vrolijken
 * @since 9 mei 2016
 */
public final class ServiceLoaderHealth extends HealthCheck {
    private final ContextListener contextListener;

    public ServiceLoaderHealth(ContextListener contextListener) {
        super();
        this.contextListener = contextListener;
    }

    @Override
    protected Result check() throws Exception {
        if (contextListener.ok) {
            return Result.healthy();
        }
        return Result.unhealthy("Not all ServiceLoaders were started successfully, check the logs");
    }
}
