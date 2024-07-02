FROM gcr.io/distroless/java21-debian12
COPY /target/altinn-rettigheter-proxy-0.0.1-SNAPSHOT.jar app.jar
CMD ["app.jar"]
