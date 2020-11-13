package com.cs.webservice.handler.auth;

import com.cs.webservice.domain.auth.AdminAuth;
import com.cs.webservice.domain.auth.EmailCertify;
import com.cs.webservice.domain.auth.UserAuth;
import com.cs.webservice.domain.auth.UserInform;
import com.cs.webservice.domain.auth.repository.AdminAuthRepository;
import com.cs.webservice.domain.auth.repository.EmailCertifyRepository;
import com.cs.webservice.domain.auth.repository.UserAuthRepository;
import com.cs.webservice.domain.auth.repository.UserInformRepository;
import com.cs.webservice.domain.campaign.Campaign;
import com.cs.webservice.dto.auth.*;
import com.cs.webservice.dto.campaign.CampaignDTO;
import com.cs.webservice.handler.BaseHandler;
import com.cs.webservice.utils.CampaignStatus;
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
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthHandlerImpl extends BaseHandler implements AuthHandler {
    private final UserAuthRepository userAuthRepository;

    private final AdminAuthRepository adminAuthRepository;

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
        resp.setCampaignNumber(CampaignNumberDTO.builder()
                .approved(1)
                .rejected(50)
                .participate(100)
                .build());

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

    @Override
    public ResponseEntity<LoginAdminAuth.Response> loginAdminAuth(LoginAdminAuth.Request req, BindingResult bindingResult) {
        var resp = new LoginAdminAuth.Response();
        if (bindingResult.hasErrors()) {
            resp.setStatus(HttpStatus.BAD_REQUEST.value());
            resp.setMessage(bindingResult.getAllErrors().toString());
            return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
        }

        // -1071 -> 존재하지 않는 admin id
        // -1072 -> 올바르지 않은 admin pw

        Optional<AdminAuth> selectResult = adminAuthRepository.findByAdminID(req.getAdminID());
        if (selectResult.isEmpty()) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1071);
            resp.setMessage("not exist admin id");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

        AdminAuth adminAuth = selectResult.get();
        if (!BCrypt.checkpw(req.getAdminPW(), adminAuth.getAdminPW())) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1072);
            resp.setMessage("incorrect admin password");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

        resp.setStatus(HttpStatus.OK.value());
        resp.setAccessToken(jwtTokenProvider.generateAccessToken(adminAuth.getUuid()));
        resp.setUserUUID(adminAuth.getUuid());
        resp.setMessage("succeed to login admin auth");
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GetUserInformsWithUUIDs.Response> getUserInformsWithUUIDs(GetUserInformsWithUUIDs.Request req, BindingResult bindingResult, String token) {
        var resp = new GetUserInformsWithUUIDs.Response();

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

        List<UserInformDTO> userInformsForResp = new ArrayList<>();
        for (String userUUID: req.getUserUUIDs()) {
            Optional<UserAuth> selectAuth = userAuthRepository.findById(userUUID);
            Optional<UserInform> selectInform = userInformRepository.findByUserAuth(UserAuth.builder().uuid(userUUID).build());
            if (selectInform.isEmpty() || selectAuth.isEmpty()) {
                resp.setStatus(HttpStatus.NOT_FOUND.value());
                resp.setMessage("user uuid list contain not exist user");
                return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
            }

            UserAuth userAuth = selectAuth.get();
            UserInform userInform = selectInform.get();
            UserInformDTO respUserInform = UserInformDTO.builder()
                    .userUUID(userAuth.getUuid())
                    .userID(userAuth.getUserID())
                    .name(userInform.getName())
                    .nickName(userInform.getNickName())
                    .email(userInform.getEmail())
                    .profileURI(userInform.getProfileURI())
                    .campaignNumber(CampaignNumberDTO.builder()
                            .approved(userInform.getApprovedNumber())
                            .rejected(userInform.getRejectedNumber())
                            .participate(userInform.getParticipationNumber())
                            .build())
                    .build();
            userInformsForResp.add(respUserInform);
        }

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to get user informs with uuid list");
        resp.setUserInforms(userInformsForResp);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
