FROM gcr.io/distroless/java21-debian12
COPY /target/altinn-rettigheter-proxy-0.0.1-SNAPSHOT.jar app.jar

ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75"
CMD ["app.jar"]
