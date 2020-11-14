package com.cs.webservice.handler.campaign;

import com.cs.webservice.domain.auth.repository.UserAuthRepository;
import com.cs.webservice.domain.campaign.Campaign;
import com.cs.webservice.domain.campaign.CampaignReport;
import com.cs.webservice.domain.campaign.CampaignTag;
import com.cs.webservice.domain.campaign.CampaignVote;
import com.cs.webservice.domain.campaign.repository.CampaignReportRepository;
import com.cs.webservice.domain.campaign.repository.CampaignRepository;
import com.cs.webservice.domain.campaign.repository.CampaignTagRepository;
import com.cs.webservice.domain.campaign.repository.CampaignVoteRepository;
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

    private final UserAuthRepository userAuthRepository;

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
        if (currentCampaigns.size() >= 3) {
            resp.setStatus(HttpStatus.CONFLICT.value());
            resp.setCode(-1061);
            resp.setMessage("you have exceeded the number of registered campaigns");
            return new ResponseEntity<>(resp, HttpStatus.CONFLICT);
        }

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

        // -1071 -> (투표 시) 이미 투표한 캠페인임
        // -1072 -> (취소 시) 투표하지 않은 캠페인임

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

        // -1081 -> (신고 시) 이미 신고한 캠페인임

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

        // -1091 -> (승인 혹은 거절 시) 이미 처리된 신고임

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
}