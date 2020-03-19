package no.nav.arbeidsgiver.altinnrettigheter.proxy.model

import com.fasterxml.jackson.annotation.JsonProperty

data class AltinnOrganisasjon(
        @JsonProperty("Name")
        val name: String,
        @JsonProperty("Type")
        val type: String,
        @JsonProperty("ParentOrganizationNumber")
        val parentOrganizationNumber: String? = null,
        @JsonProperty("OrganizationNumber")
        val organizationNumber: String,
        @JsonProperty("OrganizationForm")
        val organizationForm: String,
        @JsonProperty("Status")
        val status: String
)

data class AltinnRolle(
        @JsonProperty("RoleType")
        private var type: String,
        @JsonProperty("RoleDefinitionId")
        private val definitionId: String,
        @JsonProperty("RoleName")
        private val name: String,
        @JsonProperty("RoleDescription")
        private val description: String? = null
)