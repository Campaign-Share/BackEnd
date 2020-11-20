package com.cs.webservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

@Setter
public abstract class BaseResponse {
    @JsonProperty("status")
    private int status;
    @JsonProperty("code")
    private int code;
    @JsonProperty("message")
    private String message;
}
