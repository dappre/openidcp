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

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Checks to see if Redis is happy
 *
 * @author Friso Vrolijken
 * @since 9 mei 2016
 */
public final class JedisHealth extends HealthCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisHealth.class);
    private static final ReentrantLock checkLock = new ReentrantLock();

    private final JedisPool pool;
    private final String key;

    public JedisHealth(JedisPool pool) {
        super();
        this.key = "healthcheck@" + getComputerName();
        this.pool = pool;
    }

    private static String getComputerName() {
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME")) {
            return env.get("COMPUTERNAME");
        } else if (env.containsKey("HOSTNAME")) {
            return env.get("HOSTNAME");
        } else {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                LOGGER.error("no known computer name, this might fail", e);
                return "unknown";
            }
        }
    }

    @Override
    protected Result check() throws Exception {
        if (pool.isClosed()) {
            return Result.unhealthy("Jedis pool was closed");
        }
        try (Jedis resource = pool.getResource()) {
            if (!resource.isConnected()) {
                return Result.unhealthy("resource returned from pool is not connected");
            }
            try {
                checkLock.lock();
                long startvalue = System.currentTimeMillis();

                resource.set(key, Long.toString(startvalue));
                Long value = resource.incr(key);
                if (value.longValue() != (startvalue + 1)) {
                    Result.unhealthy("Expected value " + (startvalue + 1) + " does not match read value " + value);
                }
            } finally {
                checkLock.unlock();
            }
        }

        return Result.healthy();
    }
}
