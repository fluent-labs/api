FROM lkjaero/foreign-language-reader-api:builder as builder

# Compile the service
COPY . /app/
RUN sbt clean coverageOff api/dist

# Detect the version and unzip to /app/dist
# hadolint ignore=SC2086
RUN VERSION=$(grep -Eo "[0-9\.]+" version.sbt) && \
    echo "Detected version $VERSION" && \
    unzip /app/api/target/universal/api-$VERSION-SNAPSHOT.zip -d ./api && \
    mkdir dist && \
    mv api/api-$VERSION-SNAPSHOT/* dist

## Make sure tests are run on the correct JVM
## Changes to string methods between versions has burned us before
RUN sbt test

FROM lkjaero/foreign-language-reader-api:base as final
EXPOSE 9000
CMD ["/app/bin/api", "-Dconfig.resource=production.conf"]
COPY --from=builder /app/dist /app