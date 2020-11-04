package com.cs.webservice.dto.auth;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class GetUserInform {
    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
        @JsonProperty("user_uuid")
        private String userUUID;
        @JsonProperty("user_id")
        private String userID;
        @JsonProperty("name")
        private String name;
        @JsonProperty("nick_name")
        private String nickName;
        @JsonProperty("email")
        private String email;
        @JsonProperty("profile_uri")
        private String profileURI;
    }
}
