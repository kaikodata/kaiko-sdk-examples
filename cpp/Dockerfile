FROM ubuntu:noble

RUN apt update && \
    apt-get upgrade --yes && \
    apt-get install --yes ca-certificates

COPY build_conan/bin/app app

CMD ./app
