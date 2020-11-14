package com.cs.webservice.dto.auth;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class GetUsersSortedByParticipate {
    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
        @JsonProperty("user_informs")
        List<UserInformDTO> userInforms;
    }
}
