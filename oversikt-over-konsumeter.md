# Oversikt over konsumenter

Her er en uformell oversikt over hvem som kaller altinn-rettigheter-proxy. Den oppdateres ved behov, og er ikke garantert å være fullstending.

##
Hvem | repo |  klient/endepunkter | feilhåndtering
-----|----------|------|----------
tiltaksgjennomføring | https://github.com/navikt/tiltaksgjennomforing-api | klient | AltinnrettigheterProxyKlientFallbackException
ia-tjenester-metrikker | https://github.com/navikt/ia-tjenester-metrikker | klient | Exception
innsyn-aareg-api | https://github.com/navikt/arbeidsgiver-innsyn-aareg-api | klient | 403 i message, ellers Exception
permittering-refusjon-api | https://github.com/navikt/permittering-refusjon-api | `/ekstern/altinn/api/serviceowner/reportee` | 404 -> tom liste, Exception
sykefraværsstatistikk | https://github.com/navikt/sykefravarsstatistikk-api/ | klient | ingen?
klage-permittering-refusjon-api | https://github.com/navikt/klage-permittering-refusjon-api | ekstern/altinn/api/serviceowner/reportees | Exception
min-side-arbeidsgiver-api | https://github.com/navikt/min-side-arbeidsgiver-api | klient | 403, AltinnrettigheterProxyKlientFallbackException.ServerResponseException
notifikasjon-bruker-api | https://github.com/navikt/arbeidsgiver-notifikasjon-produsent-api | klient | samme som min-side-ag-api
permitteringsskjema-api | https://github.com/navikt/permitteringsskjema-api | `ekstern/altinn/api/serviceowner/` | RestClientException
