package com.cs.webservice.dto.auths;

import com.cs.webservice.domain.auths.UserAuth;
import com.cs.webservice.domain.auths.UserInform;
import com.cs.webservice.dto.BaseResponse;
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

        @Size(min = 11,max = 11) @NotNull @NotEmpty @Email
        @JsonProperty("email")
        private String email;
    }

    @NoArgsConstructor
    public static class Response extends BaseResponse { }
}
