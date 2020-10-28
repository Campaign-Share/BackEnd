package com.cs.webservice.dto.auths;

import com.cs.webservice.domain.auths.UserAuth;
import com.cs.webservice.domain.auths.UserInform;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.*;

public class CreateNewUser {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Request {
        @Size(min = 4, max = 20) @NotNull @NotEmpty
        @JsonProperty("user_id")
        private String userID;

        @Size(min = 4, max = 20) @NotNull @NotEmpty
        @JsonProperty("user_pw")
        private String userPW;

        @Size(min = 2, max = 5) @NotNull @NotEmpty
        @JsonProperty("name")
        private String name;

        @Size(max = 10) @NotNull
        @JsonProperty("nick_name")
        private String nickName;

        @Size(min = 11,max = 11) @NotNull @NotEmpty
        @JsonProperty("phone_number")
        @Pattern(regexp = "^010\\d{8}")
        private String phoneNumber;
    }

    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {

    }
}
