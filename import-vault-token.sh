#!/usr/bin/env sh

if test -f /var/run/secrets/nais.io/altinn/x-nav-apiKey;
then
    export ALTINN_APIGW_APIKEY=$(cat /var/run/secrets/nais.io/altinn/x-nav-apiKey)
    echo "Setting ALTINN_APIGW_APIKEY"
fi
