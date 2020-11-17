package com.cs.webservice.dto.campaign;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class GetParticipationsWithUserUUID {
    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
        @JsonProperty("participations")
        List<ParticipationDTO> participations;
    }
}
