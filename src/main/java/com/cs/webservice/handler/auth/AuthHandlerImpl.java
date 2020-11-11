package com.cs.webservice.handler.auth;

import com.cs.webservice.domain.auth.EmailCertify;
import com.cs.webservice.domain.auth.UserAuth;
import com.cs.webservice.domain.auth.UserInform;
import com.cs.webservice.domain.auth.repository.EmailCertifyRepository;
import com.cs.webservice.domain.auth.repository.UserAuthRepository;
import com.cs.webservice.domain.auth.repository.UserInformRepository;
import com.cs.webservice.dto.auth.*;
import com.cs.webservice.handler.BaseHandler;
import com.cs.webservice.utils.JwtTokenProvider;
import com.cs.webservice.utils.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthHandlerImpl extends BaseHandler implements AuthHandler {
    private final UserAuthRepository userAuthRepository;

    private final UserInformRepository userInformRepository;

    private final EmailCertifyRepository emailCertifyRepository;

    private final JwtTokenProvider jwtTokenProvider;

    private final S3Service s3Service;

    @Override
    public ResponseEntity<CreateNewUser.Response> createNewUser(CreateNewUser.Request req, BindingResult bindingResult) throws IOException {
        var resp = new CreateNewUser.Response();
        if (bindingResult.hasErrors()) {
            resp.setStatus(HttpStatus.BAD_REQUEST.value());
            resp.setMessage(bindingResult.getAllErrors().toString());
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

        // 인증되지 않은 email임 -> -1021
        // 이미 사용중인 email임 -> -1022
        // user id 중복 -> -1023
        // email 중복 -> -1024

        Optional<EmailCertify> selectResult = emailCertifyRepository.findByEmailAndCertified(req.getEmail(), true);
        if (selectResult.isEmpty()) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1021);
            resp.setMessage("that email is not certified");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

        EmailCertify certifiedEmail = selectResult.get();
        if (certifiedEmail.isUsing()) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1022);
            resp.setMessage("that email is already used");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

        if (userAuthRepository.findByUserID(req.getUserID()).isPresent()) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1023);
            resp.setMessage("user id duplicated");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

        if (userInformRepository.findByEmail(req.getEmail()).isPresent()) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1024);
            resp.setMessage("email id duplicated");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
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

        if (req.getProfile() != null) {
            String profileURI = "profiles/" + userUUID;
            userInform.setProfileURI(profileURI);
            s3Service.upload(req.getProfile(), profileURI);
        }

        userAuthRepository.save(userAuth);
        userInformRepository.save(userInform);

        certifiedEmail.setUsing(true);
        emailCertifyRepository.save(certifiedEmail);

        resp.setStatus(HttpStatus.CREATED.value());
        resp.setMessage("succeed to create new user");
        resp.setUserUUID(userUUID);
        return new ResponseEntity<>(resp, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<LoginUserAuth.Response> loginUserAuth(LoginUserAuth.Request req, BindingResult bindingResult) {
        var resp = new LoginUserAuth.Response();
        if (bindingResult.hasErrors()) {
            resp.setStatus(HttpStatus.BAD_REQUEST.value());
            resp.setMessage(bindingResult.getAllErrors().toString());
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

        // 아이디 없음 -> -1031
        // 비밀번호 오류 -> -1032

        Optional<UserAuth> selectResult = userAuthRepository.findByUserID(req.getUserID());
        if (selectResult.isEmpty()) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1031);
            resp.setMessage("not exist user id");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

        UserAuth userAuth = selectResult.get();
        if (!BCrypt.checkpw(req.getUserPW(), userAuth.getUserPW())) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1032);
            resp.setMessage("incorrect user password");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

        resp.setStatus(HttpStatus.OK.value());
        resp.setAccessToken(jwtTokenProvider.generateAccessToken(userAuth.getUuid()));
        resp.setUserUUID(userAuth.getUuid());
        resp.setMessage("succeed to login user auth");
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ChangeUserPW.Response> changeUserPW(ChangeUserPW.Request req, BindingResult bindingResult, String token, String userUUID) {
        var resp = new ChangeUserPW.Response();

        AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        if (bindingResult.hasErrors()) {
            resp.setStatus(HttpStatus.BAD_REQUEST.value());
            resp.setMessage(bindingResult.getAllErrors().toString());
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

        if (!authenticateResult.uuid.equals(userUUID)) {
            resp.setStatus(HttpStatus.FORBIDDEN.value());
            resp.setMessage("uuid in request uri is not your uuid");
            return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
        }

        Optional<UserAuth> selectResult = userAuthRepository.findById(userUUID);
        if (selectResult.isEmpty()) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("not exist user uuid");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }

        // 현재 비밀번호 일치 X -> -1041

        UserAuth userAuth = selectResult.get();
        if (!BCrypt.checkpw(req.getCurrentPW(), userAuth.getUserPW())) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1041);
            resp.setMessage("incorrect current password");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

        userAuth.setUserPW(new BCryptPasswordEncoder().encode(req.getRevisionPW()));
        userAuthRepository.save(userAuth);

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to change password");
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GetUserInform.Response> getUserInform(String token, String userUUID) {
        var resp = new GetUserInform.Response();

        AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        Optional<UserAuth> selectResult = userAuthRepository.findById(userUUID);
        if (selectResult.isEmpty()) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("not exist user uuid");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }

        UserAuth userAuth = selectResult.get();
        Optional<UserInform> selectInform = userInformRepository.findByUserAuth(userAuth);
        if (selectInform.isEmpty()) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("not exist user uuid in user inform");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }
        UserInform userInform = selectInform.get();

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to get user inform");
        resp.setUserID(userAuth.getUserID());
        resp.setUserUUID(userInform.getUserAuth().getUuid());
        resp.setName(userInform.getName());
        resp.setNickName(userInform.getNickName());
        resp.setEmail(userInform.getEmail());
        resp.setProfileURI(userInform.getProfileURI());
        resp.setCampaignNumber(new GetUserInform.CampaignNumber(1, 50, 100));

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<DeleteUser.Response> deleteUser(String token, String userUUID) {
        var resp = new DeleteUser.Response();

        AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        if (!authenticateResult.uuid.equals(userUUID)) {
            resp.setStatus(HttpStatus.FORBIDDEN.value());
            resp.setMessage("uuid in request uri is not your uuid");
            return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
        }

        Optional<UserAuth> selectAuth = userAuthRepository.findById(userUUID);
        if (selectAuth.isEmpty()) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("not exist user uuid");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }

        UserAuth userAuth = selectAuth.get();
        userInformRepository.findByUserAuth(userAuth)
                .ifPresent(userInform -> {
                    if (userInform.getProfileURI() != null) {
                        s3Service.delete(userInform.getProfileURI());
                    }
                    emailCertifyRepository.deleteByEmail(userInform.getEmail());
                });
        userAuthRepository.delete(selectAuth.get());

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to delete user");
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ChangeUserInform.Response> changeUserInform(ChangeUserInform.Request req, BindingResult bindingResult, String token, String userUUID) throws IOException {
        var resp = new ChangeUserInform.Response();

        AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        if (bindingResult.hasErrors()) {
            resp.setStatus(HttpStatus.BAD_REQUEST.value());
            resp.setMessage(bindingResult.getAllErrors().toString());
            System.out.println(bindingResult.getAllErrors().toString());
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

        if (!authenticateResult.uuid.equals(userUUID)) {
            resp.setStatus(HttpStatus.FORBIDDEN.value());
            resp.setMessage("uuid in request uri is not your uuid");
            return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
        }

        Optional<UserAuth> selectAuth = userAuthRepository.findById(userUUID);
        if (selectAuth.isEmpty()) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("not exist user uuid");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }

        // 인증되지 않은 email임 -> -1051
        // 이미 사용중인 email임 -> -1052
        // email 중복 -> -1053

        if (req.getEmail() != null) {
            Optional<EmailCertify> selectEmail = emailCertifyRepository.findByEmailAndCertified(req.getEmail(), true);
            if (selectEmail.isEmpty()) {
                resp.setStatus(HttpStatus.CONFLICT.value());
                resp.setCode(-1051);
                resp.setMessage("not certify email");
                return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
            }
            if (selectEmail.get().isUsing()) {
                resp.setStatus(HttpStatus.CONFLICT.value());
                resp.setCode(-1052);
                resp.setMessage("email is already used");
                return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
            }
        }

        if (userInformRepository.findByEmail(req.getEmail()).isPresent()) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1053);
            resp.setMessage("email duplicate error");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

        Optional<UserInform> selectInform = userInformRepository.findByUserAuth(selectAuth.get());
        if (selectInform.isEmpty()) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("not exist user uuid in user inform");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }

        UserInform userInform = selectInform.get();
        if (req.getEmail() != null) {
            emailCertifyRepository.findByEmail(userInform.getEmail())
                    .ifPresent(emailCertify -> {
                        System.out.println(emailCertify.getEmail() + "1");
                        emailCertifyRepository.deleteByEmail(emailCertify.getEmail());
                    });
            emailCertifyRepository.findByEmail(req.getEmail())
                    .ifPresent(emailCertify -> {
                        System.out.println(emailCertify.getEmail() + "2");
                        emailCertify.setUsing(true);
                        emailCertifyRepository.save(emailCertify);
                    });
            userInform.setEmail(req.getEmail());
        }
        if (req.getName() != null) {
            userInform.setName(req.getName());
        }
        if (req.getNickName() != null) {
            userInform.setNickName(req.getNickName());
        }

        if (req.getProfile() != null) {
            String profileURI = "profiles/" + userUUID;
            userInform.setProfileURI(profileURI);
            s3Service.upload(req.getProfile(), profileURI);
        }

        userInformRepository.save(userInform);

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("Succeed to change user inform");
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
