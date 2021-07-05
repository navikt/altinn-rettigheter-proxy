Altinn-proxy
============

Denne appen er en proxy mot Altinn sine tjenester til arbeidsgiver.

# Komme i gang

Koden kan kjøres som en vanlig Spring Boot-applikasjon fra AltinnrettigheterproxyApplication.
 Åpnes i browser: [http://localhost:9090/altinn-rettigheter-proxy/internal/healthcheck](http://localhost:9090/altinn-rettigheter-proxy/internal/healthcheck)

 Default spring-profil er local, og da er alle avhengigheter mocket på localhost:9091. 

## Docker
Bygg image
`docker build -t altinnproxy .`

Kjør container
`docker run -d -p 9090:9090 altinnproxy`

---

# Henvendelser

## For Nav-ansatte
* Dette Git-repositoriet eies av [Team Fellestjenester for Arbeidsgivere (tidl. Min side – arbeidsgiver)  i Produktområde arbeidsgiver](https://navno.sharepoint.com/sites/intranett-prosjekter-og-utvikling/SitePages/Produktomr%C3%A5de-arbeidsgiver.aspx).
* Slack-kanaler:
 * [#arbeidsgiver-min-side-arbeidsgiver](https://nav-it.slack.com/archives/CCNAY9FGF)
 * [#arbeidsgiver-utvikling](https://nav-it.slack.com/archives/CD4MES6BB)
 * [#arbeidsgiver-general](https://nav-it.slack.com/archives/CCM649PDH)

## For folk utenfor Nav
* Opprett gjerne en issue i Github for alle typer spørsmål
* IT-utviklerne i Github-teamet https://github.com/orgs/navikt/teams/arbeidsgiver
* IT-avdelingen i [Arbeids- og velferdsdirektoratet](https://www.nav.no/no/NAV+og+samfunn/Kontakt+NAV/Relatert+informasjon/arbeids-og-velferdsdirektoratet-kontorinformasjon)