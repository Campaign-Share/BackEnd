package com.cs.webservice.dto.auth;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public final class CampaignNumberDTO {
    @JsonProperty("approved")
    private final int approved;

    @JsonProperty("rejected")
    private final int rejected;

    @JsonProperty("participate")
    private final int participate;
}
