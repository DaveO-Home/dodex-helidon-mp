#!/bin/sh

nohup /usr/bin/envoy --log-path ./logs/logfile.log --enable-fine-grain-logging --base-id 3  -l info -c /etc/envoy/envoy.yaml &

${JAVA_HOME}/bin/java --sun-misc-unsafe-memory-access=allow -jar `pwd`/dodex-helidon-mp.jar
