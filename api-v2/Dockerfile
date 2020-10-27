FROM openjdk:8-jre-alpine3.9
RUN apk add bash
COPY target/universal/foreign-language-reader-api-1.0-SNAPSHOT /app
EXPOSE 8000
CMD ["/app/bin/foreign-language-reader-api"]