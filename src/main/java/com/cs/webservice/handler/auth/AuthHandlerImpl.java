package com.cs.webservice.handler.auth;

import com.cs.webservice.domain.auth.EmailCertify;
import com.cs.webservice.domain.auth.UserAuth;
import com.cs.webservice.domain.auth.UserInform;
import com.cs.webservice.domain.auth.repository.EmailCertifyRepository;
import com.cs.webservice.domain.auth.repository.UserAuthRepository;
import com.cs.webservice.domain.auth.repository.UserInformRepository;
import com.cs.webservice.dto.auth.CreateNewUser;
import com.cs.webservice.dto.auth.LoginUserAuth;
import com.cs.webservice.utils.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthHandlerImpl implements AuthHandler {
    private final UserAuthRepository userAuthRepository;

    private final UserInformRepository userInformRepository;

    private final EmailCertifyRepository emailCertifyRepository;

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public CreateNewUser.Response createNewUser(@Valid @RequestBody CreateNewUser.Request req, BindingResult bindingResult) {
        var resp = new CreateNewUser.Response();
        if (bindingResult.hasErrors()) {
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            resp.setMessage(bindingResult.getAllErrors().toString());
            return resp;
        }

        // 인증되지 않은 email임 -> -1021
        // 이미 사용중인 email임 -> -1022
        // user id 중복 -> -1023
        // email 중복 -> -1024

        Optional<EmailCertify> selectResult = emailCertifyRepository.findByEmailAndCertified(req.getEmail(), true);
        if (selectResult.isEmpty()) {
            resp.setStatus(HttpStatus.SC_CONFLICT);
            resp.setCode(-1021);
            resp.setMessage("that email is not certified");
            return resp;
        }

        EmailCertify certifiedEmail = selectResult.get();
        if (certifiedEmail.isUsing()) {
            resp.setStatus(HttpStatus.SC_CONFLICT);
            resp.setCode(-1022);
            resp.setMessage("that email is already used");
            return resp;
        }

        if (userAuthRepository.findByUserID(req.getUserID()).isPresent()) {
            resp.setStatus(HttpStatus.SC_CONFLICT);
            resp.setCode(-1023);
            resp.setMessage("user id duplicated");
            return resp;
        }

        if (userInformRepository.findByEmail(req.getEmail()).isPresent()) {
            resp.setStatus(HttpStatus.SC_CONFLICT);
            resp.setCode(-1024);
            resp.setMessage("email id duplicated");
            return resp;
        }

        req.setUserPW(new BCryptPasswordEncoder().encode(req.getUserPW()));

        String userUUID = userAuthRepository.getAvailableUUID();
        UserAuth userAuth = UserAuth.builder()
                .uuid(userUUID)
                .userId(req.getUserID())
                .userPW(req.getUserPW()).build();
        UserInform userInform = UserInform.builder()
                .email(req.getEmail())
                .name(req.getName())
                .nickName(req.getName())
                .build();
        userInform.setUserAuth(userAuth);

        userAuthRepository.save(userAuth);
        userInformRepository.save(userInform);

        certifiedEmail.setUsing(true);
        emailCertifyRepository.save(certifiedEmail);

        resp.setStatus(HttpStatus.SC_CREATED);
        resp.setMessage("succeed to create new user");
        resp.setUserUUID(userUUID);
        return resp;
    }

    @Override
    public LoginUserAuth.Response loginUserAuth(@Valid @RequestBody LoginUserAuth.Request req, BindingResult bindingResult) {
        var resp = new LoginUserAuth.Response();
        if (bindingResult.hasErrors()) {
            resp.setStatus(HttpStatus.SC_BAD_REQUEST);
            resp.setMessage(bindingResult.getAllErrors().toString());
            return resp;
        }

        // 아이디 없음 -> -1031
        // 비밀번호 오류 -> -1032

        Optional<UserAuth> selectResult = userAuthRepository.findByUserID(req.getUserID());
        if (selectResult.isEmpty()) {
            resp.setStatus(HttpStatus.SC_CONFLICT);
            resp.setCode(-1031);
            resp.setMessage("not exist user id");
            return resp;
        }

        UserAuth userAuth = selectResult.get();
        if (!BCrypt.checkpw(req.getUserPW(), userAuth.getUserPW())) {
            resp.setStatus(HttpStatus.SC_CONFLICT);
            resp.setCode(-1032);
            resp.setMessage("incorrect user password");
            return resp;
        }

        resp.setStatus(HttpStatus.SC_OK);
        resp.setAccessToken(jwtTokenProvider.createToken(userAuth.getUuid()));
        resp.setUserUUID(userAuth.getUuid());
        resp.setMessage("succeed to login user auth");
        return resp;
    }
}
