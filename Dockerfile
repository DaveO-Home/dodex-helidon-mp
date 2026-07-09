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

FROM envoyproxy/envoy:v1.29-latest AS builder
FROM ubuntu:latest

COPY --from=builder /usr/local/bin/envoy /usr/bin/

USER root:root

RUN apt-get update && \
    apt-get install -y openjdk-25-jre-headless && \
    apt-get clean;

RUN adduser dodex --disabled-password

RUN mkdir /envoy && mkdir /data && chown 1000 /data && mkdir /data/db && mkdir /data/h2
COPY handicap/handicap.yaml /etc/envoy/envoy.yaml
RUN chmod go+r /etc/envoy/envoy.yaml && chmod o+w /data/db && chmod o+w /data/h2

EXPOSE 8060
EXPOSE 8061
EXPOSE 9901

COPY handicap/run_dodex.sh /usr/bin
RUN chmod o+x /usr/bin/run_dodex.sh

USER dodex:dodex

RUN mkdir /home/dodex/helidon && mkdir /home/dodex/helidon/logs

VOLUME ~

COPY ./target/dodex-helidon-mp.jar /home/dodex/helidon/dodex-helidon-mp.jar
COPY ./target/libs  /home/dodex/helidon/libs

WORKDIR /home/dodex/helidon

ENV JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64/
# make helidon http run on this address
ENV DOCKER_HOST=0.0.0.0
# Can change to either "postgres" or "mariadb" or override in docker create/run
ENV DEFAULT_DB=h2
ENV USE_HANDICAP=true
ENV MODE=prod

USER dodex

CMD ["/usr/bin/bash", "/usr/bin/run_dodex.sh"]