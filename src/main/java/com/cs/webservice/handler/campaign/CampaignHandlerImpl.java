package com.cs.webservice.handler.campaign;

import com.cs.webservice.domain.auth.UserAuth;
import com.cs.webservice.domain.auth.repository.UserAuthRepository;
import com.cs.webservice.domain.auth.repository.UserInformRepository;
import com.cs.webservice.domain.campaign.*;
import com.cs.webservice.domain.campaign.repository.*;
import com.cs.webservice.dto.campaign.*;
import com.cs.webservice.handler.BaseHandler;
import com.cs.webservice.utils.CampaignStatus;
import com.cs.webservice.utils.JwtTokenProvider;
import com.cs.webservice.utils.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CampaignHandlerImpl extends BaseHandler implements CampaignHandler {
    private final CampaignRepository campaignRepository;

    private final CampaignTagRepository campaignTagRepository;

    private final CampaignVoteRepository campaignVoteRepository;

    private final CampaignReportRepository campaignReportRepository;

    private final CampaignParticipationRepository campaignParticipationRepository;

    private final UserAuthRepository userAuthRepository;

    private final UserInformRepository userInformRepository;

    private final JwtTokenProvider jwtTokenProvider;

    private final S3Service s3Service;

    public ResponseEntity<CreateNewCampaign.Response> createNewCampaign(CreateNewCampaign.Request req, BindingResult bindingResult, String token) throws IOException {
        var resp = new CreateNewCampaign.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
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

        if (req.getTags() != null) {
            String[] tags = req.getTags().split("\\|");
            if (tags.length > 5) {
                resp.setStatus(HttpStatus.BAD_REQUEST.value());
                resp.setMessage("the maximum number of tags is 5");
                return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
            }
            for (String tag : tags) {
                if (tag.length() > 20) {
                    resp.setStatus(HttpStatus.BAD_REQUEST.value());
                    resp.setMessage(tag + " is over than 20 in tags");
                    return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
                }
            }
        }

        LocalDate nowDate = LocalDate.now();
        List<Campaign> currentCampaigns = campaignRepository.findAllByUserUUIDAndEndDateGreaterThanEqual(authenticateResult.uuid, nowDate);
//        if (currentCampaigns.size() >= 3) {
//            resp.setStatus(HttpStatus.CONFLICT.value());
//            resp.setCode(-1061);
//            resp.setMessage("you have exceeded the number of registered campaigns");
//            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
//        }

        String campaignUUID = campaignRepository.getAvailableUUID();
        Campaign campaign = Campaign.builder()
                .uuid(campaignUUID)
                .userUUID(authenticateResult.uuid)
                .title(req.getTitle())
                .subTitle(req.getSubTitle())
                .introduction(req.getIntroduction())
                .participation(req.getParticipation())
                .startDate(nowDate)
                .endDate(nowDate.plusDays(req.getPeriodDay() - 1)).build();

        if (req.getPoster() != null) {
            String postURI = "campaign/posters/" + campaignUUID;
            campaign.setPostURI(postURI);
            s3Service.upload(req.getPoster(), postURI);
        }

        campaignRepository.save(campaign);

        if (req.getTags() != null) {
            for (String tag : req.getTags().split("\\|")) {
                campaignTagRepository.save(CampaignTag.builder()
                        .campaignUUID(campaignUUID)
                        .tag(tag).build());
            }
        }

        resp.setStatus(HttpStatus.CREATED.value());
        resp.setMessage("succeed to create new campaign");
        resp.setCampaignUUID(campaignUUID);
        return new ResponseEntity<>(resp, HttpStatus.CREATED);
    }

    public ResponseEntity<GetCampaignsWithUserUUID.Response> getCampaignsWithUserUUID(String token, String userUUID,
                                                                               Integer startPaging, Integer countPaging, String stateStrFilter)  {
        var resp = new GetCampaignsWithUserUUID.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
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

        if (startPaging == null) {
            startPaging = 0;
        }
        if (countPaging == null) {
            countPaging = 10;
        }

        Integer stateFilter = null;
        if (stateStrFilter != null) {
            switch (stateStrFilter) {
                case "pending":
                    stateFilter = CampaignStatus.PENDING;
                    break;
                case "approved":
                    stateFilter = CampaignStatus.APPROVED;
                    break;
                case "rejected":
                    stateFilter = CampaignStatus.REJECTED;
                    break;
            }
        }

        List<Campaign> campaigns;
        if (stateFilter != null) {
            campaigns = campaignRepository.findAllByUserUUIDAndStatusWithPagingSortedByCreatedAt(userUUID, stateFilter, startPaging, countPaging);
        } else {
            campaigns = campaignRepository.findAllByUserUUIDWithPagingSortedByCreatedAt(userUUID, startPaging, countPaging);
        }

        List<CampaignDTO> campaignsForResp = new ArrayList<>();
        campaigns.forEach(campaign -> {
            CampaignDTO respCampaigns = CampaignDTO.builder()
                    .campaignUUID(campaign.getUuid())
                    .userUUID(campaign.getUserUUID())
                    .title(campaign.getTitle())
                    .subTitle(campaign.getSubTitle())
                    .introduction(campaign.getIntroduction())
                    .participation(campaign.getParticipation())
                    .startDate(campaign.getStartDate())
                    .endDate(campaign.getEndDate())
                    .postURI(campaign.getPostURI())
                    .agreeNumber(campaign.getAgreeNumber())
                    .disAgreeNumber(campaign.getDisagreeNumber())
                    .participationNumber(campaign.getParticipationNumber()).build();
            switch (campaign.getStatus()) {
                case CampaignStatus.PENDING:
                    respCampaigns.setState("pending");
                    break;
                case CampaignStatus.APPROVED:
                    respCampaigns.setState("approved");
                    break;
                case CampaignStatus.REJECTED:
                    respCampaigns.setState("rejected");
                    break;
            }

            List<String> respCampaignTags = new ArrayList<>();
            campaignTagRepository.findAllByCampaignUUID(campaign.getUuid())
                    .forEach(campaignTag -> respCampaignTags.add(campaignTag.getTag()));

            respCampaigns.setCampaignTags(respCampaignTags);
            campaignsForResp.add(respCampaigns);
        });

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to get campaigns sorted by created time");
        resp.setCampaigns(campaignsForResp);

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    public ResponseEntity<GetCampaignsSortedByCreate.Response> getCampaignsSortedByCreate(String token, Integer startPaging, Integer countPaging,
                                                                                   String stateStrFilter, String tagFilter) {
        var resp = new GetCampaignsSortedByCreate.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        if (startPaging == null) {
            startPaging = 0;
        }
        if (countPaging == null) {
            countPaging = 10;
        }

        Integer stateFilter = null;
        if (stateStrFilter != null) {
            switch (stateStrFilter) {
            case "pending":
                stateFilter = CampaignStatus.PENDING;
                break;
            case "approved":
                stateFilter = CampaignStatus.APPROVED;
                break;
            case "rejected":
                stateFilter = CampaignStatus.REJECTED;
                break;
            }
        }

        List<Campaign> campaigns;
        if (stateFilter != null && tagFilter != null) {
            campaigns = campaignRepository.findAllByTagAndStatusWithPagingSortedByCreatedAt(tagFilter, stateFilter, startPaging, countPaging);
        } else if (stateFilter != null) {
            campaigns = campaignRepository.findAllByStatusWithPagingSortedByCreatedAt(stateFilter, startPaging, countPaging);
        } else if (tagFilter != null) {
            campaigns = campaignRepository.findAllByTagWithPagingSortedByCreatedAt(tagFilter, startPaging, countPaging);
        } else {
            campaigns = campaignRepository.findAllWithPagingSortedByCreatedAt(startPaging, countPaging);
        }

        List<CampaignDTO> campaignsForResp = new ArrayList<>();
        campaigns.forEach(campaign -> {
            CampaignDTO respCampaigns = CampaignDTO.builder()
                    .campaignUUID(campaign.getUuid())
                    .userUUID(campaign.getUserUUID())
                    .title(campaign.getTitle())
                    .subTitle(campaign.getSubTitle())
                    .introduction(campaign.getIntroduction())
                    .participation(campaign.getParticipation())
                    .startDate(campaign.getStartDate())
                    .endDate(campaign.getEndDate())
                    .postURI(campaign.getPostURI())
                    .agreeNumber(campaign.getAgreeNumber())
                    .disAgreeNumber(campaign.getDisagreeNumber())
                    .participationNumber(campaign.getParticipationNumber()).build();
            switch (campaign.getStatus()) {
            case CampaignStatus.PENDING:
                respCampaigns.setState("pending");
                break;
            case CampaignStatus.APPROVED:
                respCampaigns.setState("approved");
                break;
            case CampaignStatus.REJECTED:
                respCampaigns.setState("rejected");
                break;
            }

            List<String> respCampaignTags = new ArrayList<>();
            campaignTagRepository.findAllByCampaignUUID(campaign.getUuid())
                    .forEach(campaignTag -> respCampaignTags.add(campaignTag.getTag()));

            respCampaigns.setCampaignTags(respCampaignTags);
            campaignsForResp.add(respCampaigns);
        });

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to get campaigns sorted by created time");
        resp.setCampaigns(campaignsForResp);

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    public ResponseEntity<GetCampaignsSortedByFamous.Response> getCampaignsSortedByFamous(String token, Integer startPaging, Integer countPaging,
                                                                                          String stateStrFilter, String tagFilter) {
        var resp = new GetCampaignsSortedByFamous.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        if (startPaging == null) {
            startPaging = 0;
        }
        if (countPaging == null) {
            countPaging = 10;
        }

        Integer stateFilter = null;
        if (stateStrFilter != null) {
            switch (stateStrFilter) {
                case "pending":
                    stateFilter = CampaignStatus.PENDING;
                    break;
                case "approved":
                    stateFilter = CampaignStatus.APPROVED;
                    break;
                case "rejected":
                    stateFilter = CampaignStatus.REJECTED;
                    break;
            }
        }

        List<Campaign> campaigns;
        if (stateFilter != null && tagFilter != null) {
            campaigns = campaignRepository.findAllByTagAndStatusWithPagingSortedByFamous(tagFilter, stateFilter, startPaging, countPaging);
        } else if (stateFilter != null) {
            campaigns = campaignRepository.findAllByStatusWithPagingSortedByFamous(stateFilter, startPaging, countPaging);
        } else if (tagFilter != null) {
            campaigns = campaignRepository.findAllByTagWithPagingSortedByFamous(tagFilter, startPaging, countPaging);
        } else {
            campaigns = campaignRepository.findAllWithPagingSortedByFamous(startPaging, countPaging);
        }

        List<CampaignDTO> campaignsForResp = new ArrayList<>();
        campaigns.forEach(campaign -> {
            CampaignDTO respCampaigns = CampaignDTO.builder()
                    .campaignUUID(campaign.getUuid())
                    .userUUID(campaign.getUserUUID())
                    .title(campaign.getTitle())
                    .subTitle(campaign.getSubTitle())
                    .introduction(campaign.getIntroduction())
                    .participation(campaign.getParticipation())
                    .startDate(campaign.getStartDate())
                    .endDate(campaign.getEndDate())
                    .postURI(campaign.getPostURI())
                    .agreeNumber(campaign.getAgreeNumber())
                    .disAgreeNumber(campaign.getDisagreeNumber())
                    .participationNumber(campaign.getParticipationNumber()).build();
            switch (campaign.getStatus()) {
                case CampaignStatus.PENDING:
                    respCampaigns.setState("pending");
                    break;
                case CampaignStatus.APPROVED:
                    respCampaigns.setState("approved");
                    break;
                case CampaignStatus.REJECTED:
                    respCampaigns.setState("rejected");
                    break;
            }

            List<String> respCampaignTags = new ArrayList<>();
            campaignTagRepository.findAllByCampaignUUID(campaign.getUuid())
                    .forEach(campaignTag -> respCampaignTags.add(campaignTag.getTag()));

            respCampaigns.setCampaignTags(respCampaignTags);
            campaignsForResp.add(respCampaigns);
        });

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to get campaigns sorted by agree & disagree number");
        resp.setCampaigns(campaignsForResp);

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    public ResponseEntity<GetCampaignsSortedByRandom.Response> getCampaignsSortedByRandom(String token, Integer startPaging, Integer countPaging,
                                                                                   String stateStrFilter, String tagFilter) {
        var resp = new GetCampaignsSortedByRandom.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        if (startPaging == null) {
            startPaging = 0;
        }
        if (countPaging == null) {
            countPaging = 10;
        }

        Integer stateFilter = null;
        if (stateStrFilter != null) {
            switch (stateStrFilter) {
                case "pending":
                    stateFilter = CampaignStatus.PENDING;
                    break;
                case "approved":
                    stateFilter = CampaignStatus.APPROVED;
                    break;
                case "rejected":
                    stateFilter = CampaignStatus.REJECTED;
                    break;
            }
        }

        List<Campaign> campaigns;
        if (stateFilter != null && tagFilter != null) {
            campaigns = campaignRepository.findAllByTagAndStatusWithPagingSortedByRandom(tagFilter, stateFilter, startPaging, countPaging);
        } else if (stateFilter != null) {
            campaigns = campaignRepository.findAllByStatusWithPagingSortedByRandom(stateFilter, startPaging, countPaging);
        } else if (tagFilter != null) {
            campaigns = campaignRepository.findAllByTagWithPagingSortedByRandom(tagFilter, startPaging, countPaging);
        } else {
            campaigns = campaignRepository.findAllWithPagingSortedByRandom(startPaging, countPaging);
        }

        List<CampaignDTO> campaignsForResp = new ArrayList<>();
        campaigns.forEach(campaign -> {
            CampaignDTO respCampaigns = CampaignDTO.builder()
                    .campaignUUID(campaign.getUuid())
                    .userUUID(campaign.getUserUUID())
                    .title(campaign.getTitle())
                    .subTitle(campaign.getSubTitle())
                    .introduction(campaign.getIntroduction())
                    .participation(campaign.getParticipation())
                    .startDate(campaign.getStartDate())
                    .endDate(campaign.getEndDate())
                    .postURI(campaign.getPostURI())
                    .agreeNumber(campaign.getAgreeNumber())
                    .disAgreeNumber(campaign.getDisagreeNumber())
                    .participationNumber(campaign.getParticipationNumber()).build();
            switch (campaign.getStatus()) {
                case CampaignStatus.PENDING:
                    respCampaigns.setState("pending");
                    break;
                case CampaignStatus.APPROVED:
                    respCampaigns.setState("approved");
                    break;
                case CampaignStatus.REJECTED:
                    respCampaigns.setState("rejected");
                    break;
            }

            List<String> respCampaignTags = new ArrayList<>();
            campaignTagRepository.findAllByCampaignUUID(campaign.getUuid())
                    .forEach(campaignTag -> respCampaignTags.add(campaignTag.getTag()));

            respCampaigns.setCampaignTags(respCampaignTags);
            campaignsForResp.add(respCampaigns);
        });

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to get campaigns sorted by random");
        resp.setCampaigns(campaignsForResp);

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    public ResponseEntity<GetCampaignWithUUID.Response> getCampaignWithUUID(String token, String userUUID) {
        var resp = new GetCampaignWithUUID.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        Optional<Campaign> selectResult = campaignRepository.findByUuid(userUUID);
        if (selectResult.isEmpty()) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("campaign with that uuid is not exist");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }
        Campaign campaign = selectResult.get();

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to get campaign inform with campaign uuid");
        resp.setCampaignUUID(campaign.getUuid());
        resp.setUserUUID(campaign.getUserUUID());
        resp.setTitle(campaign.getTitle());
        resp.setSubTitle(campaign.getSubTitle());
        resp.setIntroduction(campaign.getIntroduction());
        resp.setParticipation(campaign.getParticipation());
        resp.setStartDate(campaign.getStartDate());
        resp.setEndDate(campaign.getEndDate());
        resp.setPostURI(campaign.getPostURI());
        resp.setAgreeNumber(campaign.getAgreeNumber());
        resp.setDisAgreeNumber(campaign.getDisagreeNumber());
        resp.setParticipationNumber(campaign.getParticipationNumber());
        switch (campaign.getStatus()) {
            case CampaignStatus.PENDING:
                resp.setState("pending");
                break;
            case CampaignStatus.APPROVED:
                resp.setState("approved");
                break;
            case CampaignStatus.REJECTED:
                resp.setState("rejected");
                break;
        }
        List<String> respCampaignTags = new ArrayList<>();
        campaignTagRepository.findAllByCampaignUUID(campaign.getUuid())
                .forEach(campaignTag -> respCampaignTags.add(campaignTag.getTag()));
        resp.setCampaignTags(respCampaignTags);

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    public ResponseEntity<GetCampaignsWithUUIDs.Response> getCampaignsWithUUIDs(GetCampaignsWithUUIDs.Request req, BindingResult bindingResult, String token) {
        var resp = new GetCampaignsWithUUIDs.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        List<CampaignDTO> campaignsForResp = new ArrayList<>();
        for (String campaignUUID: req.getCampaignUUIDs()) {
            Optional<Campaign> selectResult = campaignRepository.findByUuid(campaignUUID);
            if (selectResult.isEmpty()) {
                resp.setStatus(HttpStatus.NOT_FOUND.value());
                resp.setMessage("campaign uuid list contain not exist campaign");
                return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
            }

            Campaign campaign = selectResult.get();
            CampaignDTO respCampaigns = CampaignDTO.builder()
                    .campaignUUID(campaign.getUuid())
                    .userUUID(campaign.getUserUUID())
                    .title(campaign.getTitle())
                    .subTitle(campaign.getSubTitle())
                    .introduction(campaign.getIntroduction())
                    .participation(campaign.getParticipation())
                    .startDate(campaign.getStartDate())
                    .endDate(campaign.getEndDate())
                    .postURI(campaign.getPostURI())
                    .agreeNumber(campaign.getAgreeNumber())
                    .disAgreeNumber(campaign.getDisagreeNumber())
                    .participationNumber(campaign.getParticipationNumber()).build();
            switch (campaign.getStatus()) {
                case CampaignStatus.PENDING:
                    respCampaigns.setState("pending");
                    break;
                case CampaignStatus.APPROVED:
                    respCampaigns.setState("approved");
                    break;
                case CampaignStatus.REJECTED:
                    respCampaigns.setState("rejected");
                    break;
            }
            List<String> respCampaignTags = new ArrayList<>();
            campaignTagRepository.findAllByCampaignUUID(campaign.getUuid())
                    .forEach(campaignTag -> respCampaignTags.add(campaignTag.getTag()));
            respCampaigns.setCampaignTags(respCampaignTags);

            campaignsForResp.add(respCampaigns);
        }

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to get campaigns with uuid list");
        resp.setCampaigns(campaignsForResp);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    public ResponseEntity<TakeActionInCampaign.Response> takeActionInCampaign(String token, String campaignUUID, String action) {
        var resp = new TakeActionInCampaign.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        Optional<Campaign> selectCampaign = campaignRepository.findByUuid(campaignUUID);
        if (selectCampaign.isEmpty()) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("campaign with that uuid is not exists");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }
        Campaign campaign = selectCampaign.get();

        switch (action) {
        case "agree":
            if (campaignVoteRepository.findByVoterUUIDAndCampaignUUID(authenticateResult.uuid, campaignUUID).isPresent()) {
                resp.setStatus(HttpStatus.CONFLICT.value());
                resp.setCode(-1071);
                resp.setMessage("you already voted to that campaign");
                return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
            }
            campaign.setAgreeNumber(campaign.getAgreeNumber() + 1);
            campaignRepository.save(campaign);
            campaignVoteRepository.save(CampaignVote.builder()
                    .campaignUUID(campaignUUID)
                    .voterUUID(authenticateResult.uuid)
                    .agree(true).build());
            break;
        case "disagree":
            if (campaignVoteRepository.findByVoterUUIDAndCampaignUUID(authenticateResult.uuid, campaignUUID).isPresent()) {
                resp.setStatus(HttpStatus.CONFLICT.value());
                resp.setCode(-1071);
                resp.setMessage("you already voted to that campaign");
                return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
            }
            campaign.setDisagreeNumber(campaign.getDisagreeNumber() + 1);
            campaignRepository.save(campaign);
            campaignVoteRepository.save(CampaignVote.builder()
                    .campaignUUID(campaignUUID)
                    .voterUUID(authenticateResult.uuid)
                    .agree(false).build());
            break;
        case "cancel-agree":
            if (campaignVoteRepository.findByVoterUUIDAndCampaignUUIDAndAgree(authenticateResult.uuid, campaignUUID, true).isEmpty()) {
                resp.setStatus(HttpStatus.CONFLICT.value());
                resp.setCode(-1072);
                resp.setMessage("you don't agree vote to that campaign");
                return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
            }
            campaign.setAgreeNumber(campaign.getAgreeNumber() - 1);
            campaignRepository.save(campaign);
            campaignVoteRepository.deleteByVoterUUIDAndCampaignUUID(authenticateResult.uuid, campaignUUID);
            break;
        case "cancel-disagree":
            if (campaignVoteRepository.findByVoterUUIDAndCampaignUUIDAndAgree(authenticateResult.uuid, campaignUUID, false).isEmpty()) {
                resp.setStatus(HttpStatus.CONFLICT.value());
                resp.setCode(-1072);
                resp.setMessage("you don't disagree vote to that campaign");
                return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
            }
            campaign.setDisagreeNumber(campaign.getDisagreeNumber() + 1);
            campaignRepository.save(campaign);
            campaignVoteRepository.deleteByVoterUUIDAndCampaignUUID(authenticateResult.uuid, campaignUUID);
            break;
        case "approve":
            if (!authenticateResult.uuid.matches("^admin-\\d{12}")) {
                resp.setStatus(HttpStatus.FORBIDDEN.value());
                resp.setMessage("this action to campaign is only for admin");
                return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
            }
            if (campaign.getStatus() != CampaignStatus.PENDING) {
                resp.setStatus(HttpStatus.CONFLICT.value());
                resp.setCode(-1073);
                resp.setMessage("that campaign was already approved or rejected");
                return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
            }
            campaign.setStatus(CampaignStatus.APPROVED);
            campaignRepository.save(campaign);
            userInformRepository.findByUserAuth(UserAuth.builder().uuid(campaign.getUserUUID()).build()).ifPresent(userInform -> {
                userInform.setApprovedNumber(userInform.getApprovedNumber() + 1);
                userInformRepository.save(userInform);
            });
            break;
        case "reject":
            if (!authenticateResult.uuid.matches("^admin-\\d{12}")) {
                resp.setStatus(HttpStatus.FORBIDDEN.value());
                resp.setMessage("this action to campaign is only for admin");
                return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
            }
            if (campaign.getStatus() != CampaignStatus.PENDING) {
                resp.setStatus(HttpStatus.CONFLICT.value());
                resp.setCode(-1073);
                resp.setMessage("that campaign was already approved or rejected");
                return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
            }
            campaign.setStatus(CampaignStatus.REJECTED);
            campaignRepository.save(campaign);
            userInformRepository.findByUserAuth(UserAuth.builder().uuid(campaign.getUserUUID()).build()).ifPresent(userInform -> {
                userInform.setRejectedNumber(userInform.getRejectedNumber() + 1);
                userInformRepository.save(userInform);
            });
            break;
        default:
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage(action + " is not undefined action");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to take action to that campaign");
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    public ResponseEntity<ReportCampaign.Response> reportCampaign(ReportCampaign.Request req, BindingResult bindingResult, String token) {
        var resp = new ReportCampaign.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
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

        if (campaignRepository.findByUuid(req.getTargetUUID()).isEmpty()) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("campaign with that uuid is not exists");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }

        if (campaignReportRepository.findByReporterUUIDAndTargetUUID(authenticateResult.uuid, req.getTargetUUID()).isPresent()) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1081);
            resp.setMessage("you already report to that campaign");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }
        String reportUUID = campaignReportRepository.getAvailableUUID();
        campaignReportRepository.save(CampaignReport.builder()
                .uuid(reportUUID)
                .reporterUUID(authenticateResult.uuid)
                .targetUUID(req.getTargetUUID())
                .field(req.getField())
                .reason(req.getReason()).build());

        resp.setStatus(HttpStatus.CREATED.value());
        resp.setMessage("succeed to make report to that campaign");
        resp.setReportUUID(reportUUID);
        return new ResponseEntity<>(resp, HttpStatus.CREATED);
    }

    public ResponseEntity<GetCampaignReports.Response> getCampaignReports(String token, Integer startPaging, Integer countPaging, String stateStrFilter) {
        var resp = new GetCampaignReports.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        if (!authenticateResult.uuid.matches("^admin-\\d{12}")) {
            resp.setStatus(HttpStatus.FORBIDDEN.value());
            resp.setMessage("this API is only for admin");
            return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
        }

        if (startPaging == null) {
            startPaging = 0;
        }
        if (countPaging == null) {
            countPaging = 10;
        }

        List<CampaignReport> campaignReports;
        if (stateStrFilter != null) {
            switch (stateStrFilter) {
            case "pending":
                campaignReports = campaignReportRepository.findAllByHandledAndSanctionedWithPagingSortedByCreatedAt(
                        false, false, startPaging, countPaging);
                break;
            case "approved":
                campaignReports = campaignReportRepository.findAllByHandledAndSanctionedWithPagingSortedByCreatedAt(
                        true, true, startPaging, countPaging);
                break;
            case "rejected":
                campaignReports = campaignReportRepository.findAllByHandledAndSanctionedWithPagingSortedByCreatedAt(
                        true, false, startPaging, countPaging);
                break;
            default:
                campaignReports = campaignReportRepository.findAllWithPagingSortedByCreatedAt(startPaging, countPaging);
            }
        } else {
            campaignReports = campaignReportRepository.findAllWithPagingSortedByCreatedAt(startPaging, countPaging);
        }

        List<CampaignReportDTO> reportsForResp = new ArrayList<>();
        campaignReports.forEach(report -> {
            CampaignReportDTO reportForResp = CampaignReportDTO.builder()
                    .reportUUID(report.getUuid())
                    .reporterUUID(report.getReporterUUID())
                    .targetUUID(report.getTargetUUID())
                    .field(report.getField())
                    .reason(report.getReason()).build();
            if (report.isHandled()) {
                if (report.isSanctioned()) {
                    reportForResp.setState("approved");
                } else {
                    reportForResp.setState("rejected");
                }
            } else {
                reportForResp.setState("pending");
            }
            userInformRepository.findByUserAuth(UserAuth.builder().uuid(report.getReporterUUID()).build()).ifPresent(userInform ->
                    reportForResp.setUserName(userInform.getName()));
            campaignRepository.findByUuid(report.getTargetUUID()).ifPresent(campaign -> reportForResp.setCampaignTitle(campaign.getTitle()));
            reportsForResp.add(reportForResp);
        });

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to get campaign reports sorted by create time");
        resp.setCampaignReports(reportsForResp);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    public ResponseEntity<TakeActionInReport.Response> takeActionInReport(String token, String reportUUID, String action) {
        var resp = new TakeActionInReport.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        if (!authenticateResult.uuid.matches("^admin-\\d{12}")) {
            resp.setStatus(HttpStatus.FORBIDDEN.value());
            resp.setMessage("this API is only for admin");
            return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
        }

        Optional<CampaignReport> selectReport = campaignReportRepository.findByUuid(reportUUID);
        if (selectReport.isEmpty()) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("report with that uuid is not exists");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }
        CampaignReport campaignReport = selectReport.get();

        switch (action) {
        case "approve":
            if (campaignReport.isHandled()) {
                resp.setStatus(HttpStatus.CONFLICT.value());
                resp.setCode(-1091);
                resp.setMessage("that report is already handled");
                return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
            }
            campaignReport.setHandled(true);
            campaignReport.setSanctioned(true);
            campaignReportRepository.save(campaignReport);

            campaignRepository.findByUuid(campaignReport.getTargetUUID()).ifPresent(campaign -> {
                campaign.setSanctioned(true);
                campaignRepository.save(campaign);
                userAuthRepository.findById(campaign.getUserUUID()).ifPresent(userAuth -> {
                    if (userAuth.isLocked()) return;
                    LocalDate lockDate;
                    switch (userAuth.getLockNumber()) {
                        case 0:
                            lockDate = LocalDate.now().plusDays(1); break;
                        case 1:
                            lockDate = LocalDate.now().plusDays(7); break;
                        default:
                            lockDate = LocalDate.now().plusDays(30); break;
                    }
                    userAuth.setLocked(true);
                    userAuth.setLockPeriod(lockDate);
                    userAuth.setLockNumber(userAuth.getLockNumber() + 1);
                    userAuthRepository.save(userAuth);
                });
            });
            break;
        case "reject":
            if (campaignReport.isHandled()) {
                resp.setStatus(HttpStatus.CONFLICT.value());
                resp.setCode(-1091);
                resp.setMessage("that report is already handled");
                return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
            }
            campaignReport.setHandled(true);
            campaignReport.setSanctioned(false);
            campaignReportRepository.save(campaignReport);
            break;
        default:
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage(action + " is undefined action");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to take action to that campaign report");
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    public ResponseEntity<CreateNewParticipation.Response> createNewParticipation(CreateNewParticipation.Request req, BindingResult bindingResult, String token) throws IOException {
        var resp = new CreateNewParticipation.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
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

        Optional<Campaign> selectCampaign = campaignRepository.findByUuid(req.getCampaignUUID());
        if (selectCampaign.isEmpty()) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("campaign with that uuid is not exist");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }

        Campaign campaign = selectCampaign.get();
        if (campaign.getStatus() != CampaignStatus.APPROVED) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1101);
            resp.setMessage("that campaign is not approved yet");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

        if (campaignParticipationRepository.findByParticipantUUIDAndCampaignUUID(authenticateResult.uuid, req.getCampaignUUID()).isPresent()) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1102);
            resp.setMessage("you already send participation to that campaign");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

        String participationUUID = campaignParticipationRepository.getAvailableUUID();
        CampaignParticipation campaignParticipation = CampaignParticipation.builder()
                .uuid(participationUUID)
                .campaignUUID(req.getCampaignUUID())
                .participantUUID(authenticateResult.uuid)
                .introduction(req.getIntroduction()).build();

        if (req.getEvidence() != null) {
            String evidenceURI = "campaign/evidences/" + participationUUID;
            campaignParticipation.setEvidenceURI(evidenceURI);
            s3Service.uploadWithContentType(req.getEvidence(), evidenceURI, "image/jpeg");
        }

        campaignParticipationRepository.save(campaignParticipation);

        resp.setStatus(HttpStatus.CREATED.value());
        resp.setMessage("succeed to create new campaign participation");
        resp.setParticipationUUID(participationUUID);
        return new ResponseEntity<>(resp, HttpStatus.CREATED);
    }

    public ResponseEntity<GetCampaignsSortedByParticipation.Response> getCampaignsSortedByParticipation(String token, Integer startPaging, Integer countPaging) {
        var resp = new GetCampaignsSortedByParticipation.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        if (!authenticateResult.uuid.matches("^admin-\\d{12}")) {
            resp.setStatus(HttpStatus.FORBIDDEN.value());
            resp.setMessage("this API is only for admin");
            return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
        }

        if (startPaging == null) {
            startPaging = 0;
        }
        if (countPaging == null) {
            countPaging = 10;
        }

        List<CampaignDTO> campaignsForResp = new ArrayList<>();
        List<String> campaignUUID = campaignParticipationRepository.findAllWithPagingSortedByTotalPendingNumber(startPaging, countPaging);
        if (campaignUUID.size() == 0) {
            resp.setStatus(HttpStatus.OK.value());
            resp.setMessage("succeed to get campaigns sorted by total pending participation number");
            resp.setCampaigns(campaignsForResp);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        }

        campaignRepository.findAllByUUIDAndEndDateGreaterThanNowSortedByUUID(campaignUUID).forEach(campaign -> {
            CampaignDTO respCampaigns = CampaignDTO.builder()
                    .campaignUUID(campaign.getUuid())
                    .userUUID(campaign.getUserUUID())
                    .title(campaign.getTitle())
                    .subTitle(campaign.getSubTitle())
                    .introduction(campaign.getIntroduction())
                    .participation(campaign.getParticipation())
                    .startDate(campaign.getStartDate())
                    .endDate(campaign.getEndDate())
                    .postURI(campaign.getPostURI())
                    .agreeNumber(campaign.getAgreeNumber())
                    .disAgreeNumber(campaign.getDisagreeNumber())
                    .participationNumber(campaign.getParticipationNumber()).build();
            switch (campaign.getStatus()) {
                case CampaignStatus.PENDING:
                    respCampaigns.setState("pending");
                    break;
                case CampaignStatus.APPROVED:
                    respCampaigns.setState("approved");
                    break;
                case CampaignStatus.REJECTED:
                    respCampaigns.setState("rejected");
                    break;
            }

            List<String> respCampaignTags = new ArrayList<>();
            campaignTagRepository.findAllByCampaignUUID(campaign.getUuid())
                    .forEach(campaignTag -> respCampaignTags.add(campaignTag.getTag()));

            respCampaigns.setCampaignTags(respCampaignTags);
            campaignsForResp.add(respCampaigns);
        });

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to get campaigns sorted by total pending participation number");
        resp.setCampaigns(campaignsForResp);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    public ResponseEntity<GetParticipationsWithUUID.Response> getParticipationsSortedByCreate(String token, String campaignUUID,
                                                                                              Integer startPaging, Integer countPaging, String stateStrFilter) {
        var resp = new GetParticipationsWithUUID.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        if (!authenticateResult.uuid.matches("^admin-\\d{12}")) {
            resp.setStatus(HttpStatus.FORBIDDEN.value());
            resp.setMessage("this API is only for admin");
            return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
        }

        if (campaignRepository.findByUuid(campaignUUID).isEmpty()) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("campaign with that uuid is not exists");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }

        if (startPaging == null) {
            startPaging = 0;
        }
        if (countPaging == null) {
            countPaging = 10;
        }

        Integer stateFilter = null;
        if (stateStrFilter != null) {
            switch (stateStrFilter) {
                case "pending":
                    stateFilter = CampaignStatus.PENDING;
                    break;
                case "approved":
                    stateFilter = CampaignStatus.APPROVED;
                    break;
                case "rejected":
                    stateFilter = CampaignStatus.REJECTED;
                    break;
            }
        }

        List<CampaignParticipation> campaignParticipations;
        if (stateFilter != null) {
            campaignParticipations = campaignParticipationRepository.findAllByCampaignUUIDAndStateWIthPagingSortedByCreateTime(
                    campaignUUID, stateFilter, startPaging, countPaging);
        } else {
            campaignParticipations = campaignParticipationRepository.findAllByCampaignUUIDWIthPagingSortedByCreateTime(
                    campaignUUID, startPaging, countPaging);
        }

        List<ParticipationDTO> participationsForResp = new ArrayList<>();
        campaignParticipations.forEach(participation -> {
            ParticipationDTO participationForResp = ParticipationDTO.builder()
                    .participationUUID(participation.getUuid())
                    .campaignUUID(participation.getCampaignUUID())
                    .participantUUID(participation.getParticipantUUID())
                    .introduction(participation.getIntroduction())
                    .evidenceURI(participation.getEvidenceURI()).build();
            switch (participation.getState()) {
            case CampaignStatus.PENDING:
                participationForResp.setState("pending");
                break;
            case CampaignStatus.APPROVED:
                participationForResp.setState("approved");
                break;
            case CampaignStatus.REJECTED:
                participationForResp.setState("rejected");
                break;
            }
            userInformRepository.findByUserAuth(UserAuth.builder().uuid(participation.getParticipantUUID()).build()).ifPresent(userInform ->
                    participationForResp.setUserName(userInform.getName()));
            campaignRepository.findByUuid(participation.getCampaignUUID()).ifPresent(campaign -> participationForResp.setCampaignTitle(campaign.getTitle()));
            participationsForResp.add(participationForResp);
        });

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to get participation informs of that campaign uuid");
        resp.setParticipations(participationsForResp);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    public ResponseEntity<GetParticipationWithUUID.Response> getParticipationWithUUID(String token, String participationUUID) {
        var resp = new GetParticipationWithUUID.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        if (!authenticateResult.uuid.matches("^admin-\\d{12}")) {
            resp.setStatus(HttpStatus.FORBIDDEN.value());
            resp.setMessage("this API is only for admin");
            return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
        }

        Optional<CampaignParticipation> selectParticipation = campaignParticipationRepository.findById(participationUUID);
        if (selectParticipation.isEmpty()) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("participation with that uuid is not exists");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }
        CampaignParticipation campaignParticipation = selectParticipation.get();

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to get campaign participation inform with uuid");
        resp.setParticipationUUID(campaignParticipation.getUuid());
        resp.setCampaignUUID(campaignParticipation.getCampaignUUID());
        resp.setParticipantUUID(campaignParticipation.getParticipantUUID());
        resp.setIntroduction(campaignParticipation.getIntroduction());
        resp.setEvidenceURI(campaignParticipation.getEvidenceURI());
        switch (campaignParticipation.getState()) {
            case CampaignStatus.PENDING:
                resp.setState("pending");
                break;
            case CampaignStatus.APPROVED:
                resp.setState("approved");
                break;
            case CampaignStatus.REJECTED:
                resp.setState("rejected");
                break;
        }
        userInformRepository.findByUserAuth(UserAuth.builder().uuid(campaignParticipation.getParticipantUUID()).build()).ifPresent(userInform ->
                resp.setUserName(userInform.getName()));
        campaignRepository.findByUuid(campaignParticipation.getCampaignUUID()).ifPresent(campaign ->
                resp.setCampaignTitle(campaign.getTitle()));

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    public ResponseEntity<TakeActionInParticipation.Response> takeActionInParticipation(String token, String participationUUID, String action) {
        var resp = new TakeActionInParticipation.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
        if (!authenticateResult.authorized) {
            resp.setStatus(HttpStatus.UNAUTHORIZED.value());
            resp.setCode(authenticateResult.code);
            resp.setMessage(authenticateResult.message);
            return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
        }

        if (!authenticateResult.uuid.matches("^admin-\\d{12}")) {
            resp.setStatus(HttpStatus.FORBIDDEN.value());
            resp.setMessage("this API is only for admin");
            return new ResponseEntity<>(resp, HttpStatus.FORBIDDEN);
        }

        Optional<CampaignParticipation> selectParticipation = campaignParticipationRepository.findById(participationUUID);
        if (selectParticipation.isEmpty()) {
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage("campaign participations with that uuid is not exist");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }
        CampaignParticipation campaignParticipation = selectParticipation.get();

        // (수락 및 거절 시) -1111 -> 이미 수락 또는 거절 된 캠페인 인증임

        switch (action) {
        case "approve":
            if (campaignParticipation.getState() != CampaignStatus.PENDING) {
                resp.setStatus(HttpStatus.CONFLICT.value());
                resp.setCode(-1111);
                resp.setMessage("that participation is already approved or rejected");
                return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
            }
            campaignParticipation.setState(CampaignStatus.APPROVED);
            campaignParticipationRepository.save(campaignParticipation);
            campaignRepository.findByUuid(campaignParticipation.getCampaignUUID()).ifPresent(campaign -> {
                campaign.setParticipationNumber(campaign.getParticipationNumber() + 1);
                campaignRepository.save(campaign);
            });
            userInformRepository.findByUserAuth(UserAuth.builder().uuid(campaignParticipation.getParticipantUUID()).build()).ifPresent(userInform -> {
                userInform.setParticipationNumber(userInform.getParticipationNumber() + 1);
                userInformRepository.save(userInform);
            });
            break;
        case "reject":
            if (campaignParticipation.getState() != CampaignStatus.PENDING) {
                resp.setStatus(HttpStatus.CONFLICT.value());
                resp.setCode(-1111);
                resp.setMessage("that participation is already approved or rejected");
                return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
            }
            campaignParticipation.setState(CampaignStatus.REJECTED);
            campaignParticipationRepository.save(campaignParticipation);
            break;
        default:
            resp.setStatus(HttpStatus.NOT_FOUND.value());
            resp.setMessage(action + " is undefined action");
            return new ResponseEntity<>(resp, HttpStatus.NOT_FOUND);
        }

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to take action to that campaign participation");
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }


    public ResponseEntity<GetParticipateCampaigns.Response> getParticipateCampaigns(String token, String userUUID, Integer startPaging, Integer countPaging) {
        var resp = new GetParticipateCampaigns.Response();

        BaseHandler.AuthenticateResult authenticateResult = checkIfAuthenticated(token, jwtTokenProvider);
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

        if (startPaging == null) {
            startPaging = 0;
        }
        if (countPaging == null) {
            countPaging = 10;
        }

        List<String> campaignUUIDs = campaignParticipationRepository.
                        findCampaignUUIDByUserUUIDWithPagingSortedByUpdateTime(userUUID, startPaging, countPaging);
        List<CampaignDTO> campaignsForResp = new ArrayList<>();

        if (campaignUUIDs.size() == 0) {
            resp.setStatus(HttpStatus.OK.value());
            resp.setMessage("succeed to get participate campaigns informs with user uuid");
            resp.setCampaigns(campaignsForResp);
            return new ResponseEntity<>(resp, HttpStatus.OK);
        }

        campaignRepository.findAllByUUIDSortedByUUID(campaignUUIDs).forEach(campaign -> {
            CampaignDTO respCampaigns = CampaignDTO.builder()
                    .campaignUUID(campaign.getUuid())
                    .userUUID(campaign.getUserUUID())
                    .title(campaign.getTitle())
                    .subTitle(campaign.getSubTitle())
                    .introduction(campaign.getIntroduction())
                    .participation(campaign.getParticipation())
                    .startDate(campaign.getStartDate())
                    .endDate(campaign.getEndDate())
                    .postURI(campaign.getPostURI())
                    .agreeNumber(campaign.getAgreeNumber())
                    .disAgreeNumber(campaign.getDisagreeNumber())
                    .participationNumber(campaign.getParticipationNumber()).build();
            switch (campaign.getStatus()) {
                case CampaignStatus.PENDING:
                    respCampaigns.setState("pending");
                    break;
                case CampaignStatus.APPROVED:
                    respCampaigns.setState("approved");
                    break;
                case CampaignStatus.REJECTED:
                    respCampaigns.setState("rejected");
                    break;
            }

            List<String> respCampaignTags = new ArrayList<>();
            campaignTagRepository.findAllByCampaignUUID(campaign.getUuid())
                    .forEach(campaignTag -> respCampaignTags.add(campaignTag.getTag()));

            respCampaigns.setCampaignTags(respCampaignTags);
            campaignsForResp.add(respCampaigns);
        });

        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage("succeed to get participate campaigns informs with user uuid");
        resp.setCampaigns(campaignsForResp);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}