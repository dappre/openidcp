<!-- 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Qiy OpenIdConnect Provider 

How to start the Qiy OpenIdConnect Provider  application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/openidcp-0.0.7-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8093/oidcp/.well-known/openid-configuration`

Health Check
---

To see your applications health enter url `http://localhost:8093/oidcp/admin/healthcheck`
