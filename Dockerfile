FROM openjdk:8-jdk-alpine as builder
MAINTAINER reader@lucaskjaerozhang.com

ENV SBT_VERSION 1.4.0
ENV INSTALL_DIR /usr/local
ENV SBT_HOME /usr/local/sbt
ENV PATH ${PATH}:${SBT_HOME}/bin

# Install sbt
RUN apk add --no-cache --update bash wget && mkdir -p "$SBT_HOME" && \
    wget -qO - --no-check-certificate "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" |  tar xz -C $INSTALL_DIR && \
    echo -ne "- with sbt $SBT_VERSION\n" >> /root/.built

# Cache dependencies
WORKDIR /app
COPY project project
COPY build.sbt build.sbt
RUN sbt compile

# Compile the service
COPY . /app/
RUN sbt clean coverageOff dist
RUN unzip /app/api/target/universal/api-0.1.0-SNAPSHOT.zip

## Make sure tests are run on the correct JVM
## Changes to string methods between versions has burned us before
# TODO ENABLE ME AGAIN when we've fixed dependency security
# RUN sbt test

FROM openjdk:8-jdk-alpine as final
WORKDIR /app
RUN apk add bash
EXPOSE 9000
CMD ["/app/bin/api", "-Dconfig.resource=production.conf"]
COPY --from=builder /app/api-0.1.0-SNAPSHOT /app
