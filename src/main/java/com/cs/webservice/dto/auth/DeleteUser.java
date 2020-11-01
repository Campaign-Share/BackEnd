package com.cs.webservice.dto.auth;

import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class DeleteUser {
    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse { }
}
