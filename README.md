# Altinn-proxy

## Utfasing og migrering til Altinn 3

Denne applikasjonen er en proxy mot Altinn 2 api for tilganger. Altinn 2 APIer er under avvikling og alle team må migrere over til altinn 3 innen dette arbeidet er ferdig.
[Plan for utfasing](https://github.com/orgs/digdir/projects/8/views/5) av altinn 2 tilsier det vil være ferdig innen juni 2026.

Dere kan allerede i dag bytte ut altinn 2 med altinn 3 uten å migrere noe, og vi har en ny applikasjon [arbeidsgiver-altinn-tilganger](https://github.com/navikt/arbeidsgiver-altinn-tilganger) som kan hjelpe med dette arbeidet.
Den nye applikasjonen er også plug n play med ny [virksomhetsvelger](https://github.com/navikt/virksomhetsvelger/)

Vi bistår gjerne med å hjelpe dere ut av altinn 2 / `altinn-rettigheter-proxy` og over til altinn 3 / `arbeidsgiver-altinn-tilganger`.

Ta kontakt med oss på #team-fager hvis dere har spørsmål eller ønsker hjelp til å migrere.

## Om altinn-rettigheter-proxy

Denne appen er en proxy mot Altinn sine tjenester til arbeidsgiver.
Dere må bruke [TokenX](https://doc.nais.io/security/auth/tokenx/) når dere kaller oss.

Vi har et java/kotlin bibliotek [altinn-rettigheter-proxy-klient](https://github.com/navikt/altinn-rettigheter-proxy-klient) som gjør det lettere å bruke denne proxyen. Den har
bl.a. støtte for fall-back til Altinns API.

## Hvordan ta i bruk proxyen (GCP)
Dere bruker [service discovery](https://doc.nais.io/clusters/service-discovery/) for å snakke med altinn-rettigheter-proxy. 

Den fulle URLen er `http://altinn-rettigheter-proxy.arbeidsgiver.svc.cluster.local`. For å bruke denne, så kreves
det at deres app har satt outbound access policy:
```yaml
accessPolicy:
  outbound:
    rules:
      - application: altinn-rettigheter-proxy
        namespace: arbeidsgiver
        cluster: dev-gcp/prod-gcp
```
og tilsvarende, må vi legge til dere i vår inbound access policy i [nais/prod-gcp.yaml](https://github.com/navikt/altinn-rettigheter-proxy/blob/main/nais/prod-gcp.yaml) og [nais/dev-gcp.yaml](https://github.com/navikt/altinn-rettigheter-proxy/blob/main/nais/dev-gcp.yaml)
```yaml
accessPolicy:
  inbound:
    rules:
      - application: DERES_APPLIKASJON
        namespace: DERES_NAMESPACE
        cluster: dev-gcp/prod-gcp
```

I dev, så er det også en vanlig ingress tilgjengelig, `https://altinn-rettigheter-proxy.intern.dev.nav.no/altinn-rettigheter-proxy`, som dere kan bruke uten å måtte oppdatere vår access policy.

## Hvordan ta i bruk proxyen (FSS)
Fra FSS kan dere nå oss med ingressen `https://altinn-rettigheter-proxy.intern.nav.no/altinn-rettigheter-proxy/`. I dev er URL-en `https://altinn-rettigheter-proxy.intern.dev.nav.no/altinn-rettigheter-proxy`.
For at den skal fungere, må dere være lagt inn i access policy-en vår i [nais/prod-gcp.yaml](https://github.com/navikt/altinn-rettigheter-proxy/blob/main/nais/prod-gcp.yaml) og [nais/dev-gcp.yaml](https://github.com/navikt/altinn-rettigheter-proxy/blob/main/nais/dev-gcp.yaml), slik:
```yaml
accessPolicy:
  inbound:
    rules:
      - application: DERES_APPLIKASJON
        namespace: DERES_NAMESPACE
        cluster: dev-gcp/prod-gcp
```
Dere burde også legge på outbound i deres app, så kan dere bruke TokenX og er klare for når dere migrerer ut av FSS over til GCP.
```yaml
accessPolicy:
  outbound:
    rules:
      - application: altinn-rettigheter-proxy
        namespace: fager
        cluster: dev-gcp/prod-gcp
```

## Kjøre lokalt: komme i gang

Koden kan kjøres som en vanlig Spring Boot-applikasjon fra AltinnrettigheterproxyApplication.
 Åpnes i browser: [http://localhost:9090/altinn-rettigheter-proxy/internal/healthcheck](http://localhost:9090/altinn-rettigheter-proxy/internal/healthcheck)

 Default spring-profil er local, og da er alle avhengigheter mocket på localhost:9091. 

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
