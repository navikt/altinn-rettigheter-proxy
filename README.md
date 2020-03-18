Altinn-proxy
============

Denne appen er en proxy mot Altinn sine tjenester til arbeidsgiver.

# Komme i gang

Koden kan kjøres som en vanlig Spring Boot-applikasjon fra AltinnrettigheterproxyApplication.
 Åpnes i browser: [http://localhost:8080/altinn-rettigheter-proxy/internal/healthcheck](http://localhost:8080/altinn-rettigheter-proxy/internal/healthcheck)

 Default spring-profil er local, og da er alle avhengigheter mocket på localhost:8081. 

## Docker
Bygg image
`docker build -t altinnproxy .`

Kjør container
`docker run -d -p 8080:8080 altinnproxy`

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

* Lars Andreas Tveiten, lars.andreas.van.woensel.kooy.tveiten@nav.no
* Malaz Alkoj, malaz.alkoj@nav.no
* Thomas Dufourd, thomas.dufourd@nav.no

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #arbeidsgiver-teamia.
