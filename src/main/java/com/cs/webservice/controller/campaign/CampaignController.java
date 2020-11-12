package com.cs.webservice.controller.campaign;

import com.cs.webservice.dto.campaign.*;
import com.cs.webservice.handler.campaign.CampaignHandlerImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1")
public class CampaignController {
    private final CampaignHandlerImpl campaignHandler;

    @PostMapping(path = "/campaigns", consumes = {"multipart/form-data"})
    public ResponseEntity<CreateNewCampaign.Response> createNewCampaign(@Valid @ModelAttribute CreateNewCampaign.Request req, BindingResult bindingResult,
                                                        @RequestHeader(value = "Authorization", required = false) String token) throws IOException {
        return campaignHandler.createNewCampaign(req, bindingResult, token);
    }

    @GetMapping(path = "/users/uuid/{user_uuid}/campaigns")
    public ResponseEntity<GetCampaignsWithUserUUID.Response> getCampaignsWithUserUUID(
            @RequestHeader(value = "Authorization", required = false) String token, @PathVariable("user_uuid") String userUUID,
            @RequestParam(value = "start", required = false) Integer startPaging, @RequestParam(value = "count", required = false) Integer countPaging,
            @RequestParam(value = "state", required = false) String stateFilter) {
        return campaignHandler.getCampaignsWithUserUUID(token, userUUID, startPaging, countPaging, stateFilter);
    }

    @GetMapping(path = "/campaigns/sorted-by/create-time")
    public ResponseEntity<GetCampaignsSortedByCreate.Response> getCampaignsSortedByCreate(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "start", required = false) Integer startPaging, @RequestParam(value = "count", required = false) Integer countPaging,
            @RequestParam(value = "state", required = false) String stateFilter, @RequestParam(value = "tag", required = false) String tagFilter) {
        return campaignHandler.getCampaignsSortedByCreate(token, startPaging, countPaging, stateFilter, tagFilter);
    }

    @GetMapping(path = "/campaigns/sorted-by/famous")
    public ResponseEntity<GetCampaignsSortedByFamous.Response> getCampaignsSortedByFamous(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "start", required = false) Integer startPaging, @RequestParam(value = "count", required = false) Integer countPaging,
            @RequestParam(value = "state", required = false) String stateFilter, @RequestParam(value = "tag", required = false) String tagFilter) {
        return campaignHandler.getCampaignsSortedByFamous(token, startPaging, countPaging, stateFilter, tagFilter);
    }

    @GetMapping(path = "/campaigns/sorted-by/random")
    public ResponseEntity<GetCampaignsSortedByRandom.Response> getCampaignsSortedByRandom(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(value = "start", required = false) Integer startPaging, @RequestParam(value = "count", required = false) Integer countPaging,
            @RequestParam(value = "state", required = false) String stateFilter, @RequestParam(value = "tag", required = false) String tagFilter) {
        return campaignHandler.getCampaignsSortedByRandom(token, startPaging, countPaging, stateFilter, tagFilter);
    }
}
