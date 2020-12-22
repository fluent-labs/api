FROM lkjaero/foreign-language-reader-api:builder as builder

# Compile the service
COPY . /app/
RUN sbt clean coverageOff dist
RUN unzip /app/api/target/universal/api-*.zip -d ./api

## Make sure tests are run on the correct JVM
## Changes to string methods between versions has burned us before
RUN sbt test

FROM lkjaero/foreign-language-reader-api:base as final
EXPOSE 9000
CMD ["/app/bin/api", "-Dconfig.resource=production.conf"]
COPY --from=builder /app/api /app
