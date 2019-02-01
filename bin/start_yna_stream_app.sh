#!/bin/bash

date_time=`date +'%Y%m%d_%H%M%S'`
jar=scrapper-1.0-SNAPSHOT.jar

#java -jar \
#    -Xmx128m \
#    -XX:+UseG1GC \
#    -XX:G1HeapRegionSize=8M \
#    -XX:+UseGCOverheadLimit \
#    -XX:+ExplicitGCInvokesConcurrent \
#    -XX:+HeapDumpOnOutOfMemoryError \
#    -XX:+ExitOnOutOfMemoryError \
#    -Dcom.sun.management.jmxremote \
#    -Dcom.sun.management.jmxremote.authenticate=false \
#    -Djava.rmi.server.hostname=`hostname -i` \
#    -Dcom.sun.management.jmxremote.port=9999 \
#    -Dcom.sun.management.jmxremote.ssl=false \
#    batch-scrapper-1.0-SNAPSHOT.jar yna_stream \
#    1> yna_stream_${date_time}.log \
#    2> yna_stream_${date_time}.err

# -Xloggc:${date_time}_gc.log \

java -jar \
    -Xmx128m \
    -Xms128m \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:+ExitOnOutOfMemoryError \
    -XX:HeapDumpPath=logs \
    -XX:+PrintGC \
    -XX:+PrintGCDetails \
    -XX:+PrintGCDateStamps \
    -XX:-TraceClassUnloading \
    -XX:-TraceClassLoading \
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.port=9999 \
    -Dcom.sun.management.jmxremote.ssl=false \
    -Djava.rmi.server.hostname=`hostname -i` \
    ${jar} yna_stream \
    1> yna_stream_${date_time}.log \
    2> yna_stream_${date_time}.err