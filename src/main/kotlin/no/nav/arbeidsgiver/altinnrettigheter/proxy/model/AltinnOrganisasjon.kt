package no.nav.arbeidsgiver.altinnrettigheter.proxy.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class AltinnOrganisasjon(
        @JsonProperty("Name")
        val name: String,
        @JsonProperty("Type")
        val type: String,
        @JsonProperty("ParentOrganizationNumber")
        val parentOrganizationNumber: String? = null,
        @JsonProperty("OrganizationNumber")
        val organizationNumber: String?,
        @JsonProperty("OrganizationForm")
        val organizationForm: String?,
        @JsonProperty("Status")
        val status: String?
) : Serializable