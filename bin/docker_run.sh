#!/bin/bash

docker run \
    -d \
    -it \
    --rm \
    --cpus="1" \
    --memory="512m" \
    --add-host sandbox-postgres:172.18.0.21 \
    --ip 172.18.0.101 \
    --net sandbox-net \
    -e START_XVFB=false \
    -v /dev/shm:/dev/shm \
    --name sandbox-scrapper-yna \
    sandbox-scrapper-yna
