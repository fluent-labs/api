FROM openjdk:14-jdk-alpine3.10 as builder
LABEL maintainer="reader@lucaskjaerozhang.com"

ENV SBT_VERSION 1.4.0
ENV INSTALL_DIR /usr/local
ENV SBT_HOME /usr/local/sbt
ENV PATH ${PATH}:${SBT_HOME}/bin

# Sadly needed because package publishing requires this for all lifecycle steps.
ENV GITHUB_TOKEN faketoken

# Install sbt
RUN apk add --no-cache --update bash wget && mkdir -p "$SBT_HOME" && \
    wget -qO - --no-check-certificate "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" |  tar xz -C $INSTALL_DIR && \
    echo "- with sbt $SBT_VERSION\n" >> /root/.built

# Cache dependencies
WORKDIR /app
COPY project project
COPY build.sbt build.sbt
RUN sbt compile

# Compile the service
COPY . /app/
RUN sbt clean coverageOff dist
RUN unzip /app/api/target/universal/api-*.zip -d ./api

## Make sure tests are run on the correct JVM
## Changes to string methods between versions has burned us before
RUN sbt test

FROM openjdk:14-jdk-alpine3.10 as final
WORKDIR /app
RUN apk add bash
EXPOSE 9000
CMD ["/app/bin/api", "-Dconfig.resource=production.conf"]
COPY --from=builder /app/api /app
