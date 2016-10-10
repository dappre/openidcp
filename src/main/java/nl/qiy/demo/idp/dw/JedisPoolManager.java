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

package nl.qiy.demo.idp.dw;

import com.google.common.base.Preconditions;

import io.dropwizard.lifecycle.Managed;
import nl.qiy.openid.op.spi.impl.demo.JedisConfiguration;
import redis.clients.jedis.JedisPool;

/**
 * Manager that should start and stop the JedisPool
 *
 * @author Friso Vrolijken
 * @since 16 sep. 2016
 */
public class JedisPoolManager implements Managed {
    /**
     * The actual pool object being managed
     */
    public final JedisPool jedisPool;

    /**
     * Constructor for JedisPoolManager
     * 
     * @param jedisConfiguration
     *            configures Jedis to talk to Redis
     */
    public JedisPoolManager(JedisConfiguration jedisConfiguration) {
        JedisConfiguration jc = jedisConfiguration;
        jedisPool = new JedisPool(jc.poolConfiguration, jc.host, jc.port, jc.timeout, jc.password, jc.database,
                jc.clientName, jc.ssl);
    }

    @Override
    public void start() throws Exception {
        Preconditions.checkState(!jedisPool.isClosed(), "Pool is closed!");

    }

    @Override
    public void stop() throws Exception {
        jedisPool.close();
    }
}
