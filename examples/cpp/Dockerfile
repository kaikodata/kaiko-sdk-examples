FROM ubuntu:focal-20210921

RUN apt update && \
    apt-get upgrade --yes && \
    apt-get install --yes ca-certificates

COPY build/app app

CMD ./app
