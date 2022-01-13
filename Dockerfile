FROM navikt/java:17
COPY import-vault-secrets.sh /init-scripts
COPY /target/altinn-rettigheter-proxy-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
