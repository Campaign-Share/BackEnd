package com.cs.webservice.handler;

import com.cs.webservice.utils.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;

import java.util.Map;

public class BaseHandler {
    public static class AuthenticateResult {
        public boolean authorized;
        public int code;
        public String message;
        public String uuid;
    }

    public AuthenticateResult checkIfAuthenticated(String token, JwtTokenProvider jwtTokenProvider) {
        // 401 unauthorized
        // -101 -> 존재 X
        // -102 -> 토큰이 옳바른 형식이 아님
        // -103 -> 지원하는 토큰의 타입이 아님
        // -104 -> 유효하지 않은 토큰
        // -105 -> 만료된 토큰
        // -106 -> 해당 API에서 지원하는 토큰이 아님

        AuthenticateResult authResult = new AuthenticateResult();
        authResult.authorized = false;

        if (token == null || token.equals("")) {
            authResult.code = -101;
            authResult.message = "please set token in Authorization of request header";
            return authResult;
        }

        if (token.split(" ").length != 2) {
            authResult.code = -102;
            authResult.message = "invalid format of token";
            return authResult;
        }

        if (!jwtTokenProvider.isSupportedAuthorization(token)) {
            authResult.code = -103;
            authResult.message = "unsupported token type";
            return authResult;
        }

        String uuid = null;
        try {
            Map<String, Object> subject = jwtTokenProvider.getSubjectFrom(token.split(" ")[1]);
            uuid = subject.get("uuid").toString();
        } catch (ExpiredJwtException e) {
            authResult.code = -105;
            authResult.message = "expired jwt token";
            return authResult;
        } catch (java.lang.NullPointerException e) {
            authResult.code = -106;
            authResult.message = "invalid claim for this API (uuid not exists)";
            return authResult;
        } catch (Exception e) {
            authResult.code = -104;
            authResult.message = "invalid jwt token";
            return authResult;
        }

        authResult.authorized = true;
        authResult.uuid = uuid;
        return authResult;
    }
}
