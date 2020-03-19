FROM navikt/java:11
COPY import-vault-token.sh /init-scripts
COPY /target/altinn-rettigheter-proxy-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
