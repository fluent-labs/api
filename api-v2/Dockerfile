FROM openjdk:8-jdk-alpine3.9 as builder

ENV SBT_VERSION 1.3.10
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
COPY build.sbt.bak build.sbt
RUN sbt compile

# Compile the service
COPY . /app
RUN sbt clean coverageOff dist && \
    unzip /app/api-v2/target/universal/definitions-0.1.0-SNAPSHOT.zip

FROM openjdk:8-jre-alpine3.9 as final
WORKDIR /app
RUN apk add bash
EXPOSE 9000
CMD ["/app/bin/foreign-language-reader-api"]
COPY --from=builder /app/foreign-language-reader-api-1.0-SNAPSHOT /app
