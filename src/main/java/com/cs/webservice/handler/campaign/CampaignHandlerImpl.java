package com.cs.webservice.handler.campaign;

import com.cs.webservice.domain.campaign.Campaign;
import com.cs.webservice.domain.campaign.CampaignTag;
import com.cs.webservice.domain.campaign.repository.CampaignRepository;
import com.cs.webservice.domain.campaign.repository.CampaignTagRepository;
import com.cs.webservice.dto.campaign.CreateNewCampaign;
import com.cs.webservice.dto.campaign.GetCampaignsSortedByCreate;
import com.cs.webservice.dto.campaign.GetCampaignsWithUserUUID;
import com.cs.webservice.handler.BaseHandler;
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

@Service
@RequiredArgsConstructor
public class CampaignHandlerImpl extends BaseHandler implements CampaignHandler {
    private final CampaignRepository campaignRepository;

    private final CampaignTagRepository campaignTagRepository;

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

        // 캠페인 등록 가능 숫자(3) 이상 -> -1061

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

    public ResponseEntity<GetCampaignsWithUserUUID.Response> getCampaignsWithUserUUID(String token, String userUUID) {
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

        List<GetCampaignsWithUserUUID.Campaign> campaigns = new ArrayList<>();
        campaignRepository.findAllByUserUUID(authenticateResult.uuid)
                .forEach(campaign -> {
                    GetCampaignsWithUserUUID.Campaign respCampaigns = GetCampaignsWithUserUUID.Campaign.builder()
                            .campaignUUID(campaign.getUuid())
                            .userUUID(campaign.getUserUUID())
                            .title(campaign.getTitle())
                            .subTitle(campaign.getSubTitle())
                            .introduction(campaign.getIntroduction())
                            .participation(campaign.getParticipation())
                            .startDate(campaign.getStartDate())
                            .endDate(campaign.getEndDate())
                            .postURI(campaign.getPostURI()).build();

                    List<String> respCampaignTags = new ArrayList<>();
                    campaignTagRepository.findAllByCampaignUUID(campaign.getUuid())
                            .forEach(campaignTag -> respCampaignTags.add(campaignTag.getTag()));

                    respCampaigns.setCampaignTags(respCampaignTags);
                    campaigns.add(respCampaigns);
                });

        resp.setStatus(HttpStatus.OK.value());
        resp.setCampaigns(campaigns);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}