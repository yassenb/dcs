#!/bin/sh

rmiregistry &
java -Djava.rmi.server.hostname=192.168.1.3 -Djava.rmi.server.codebase=file:`pwd`/lib/executor.jar -Djava.security.policy=server.policy -jar lib/service.jar
