#
# Copyright (c) 2018, 2020 Oracle and/or its affiliates.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# 1st stage, build the app
FROM maven:3.9.5-amazoncorretto-21 as build

WORKDIR /helidon

# Create a first layer to cache the "Maven World" in the local repository.
# Incremental docker builds will always resume after that, unless you update
# the pom
ADD pom.xml .
RUN mvn package -Dmaven.test.skip -Declipselink.weave.skip

# Do the Maven build!
# Incremental docker builds will resume here when you change sources
ADD src src
RUN mvn package -DskipTests

RUN echo "done!"

# 2nd stage, build the runtime image
FROM openjdk:21
WORKDIR /helidon

# Copy the binary built in the 1st stage
COPY --from=build /helidon/target/dodex-mp.jar ./
COPY --from=build /helidon/target/libs ./libs

CMD ["java", "-jar", "dodex-mp.jar"]

EXPOSE 8060

# You may require a Docker network (docker network create testnet) if database is running in a container
