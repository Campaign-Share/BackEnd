package com.cs.webservice.handler;

public class BaseHandler {
    public static class AuthenticateResult {
        public boolean authorized;
        public int code;
        public String message;
        public String uuid;
    }
}
