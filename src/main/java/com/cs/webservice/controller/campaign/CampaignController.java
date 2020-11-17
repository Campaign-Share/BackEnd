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
    public ResponseEntity<GetCampaignsWithUserUUID.Response> getCampaignsWithUserUUID(@RequestHeader(value = "Authorization", required = false) String token,
                                                                                      @PathVariable("user_uuid") String userUUID,
                                                                                      @RequestParam(value = "start", required = false) Integer startPaging,
                                                                                      @RequestParam(value = "count", required = false) Integer countPaging,
                                                                                      @RequestParam(value = "state", required = false) String stateFilter) {
        return campaignHandler.getCampaignsWithUserUUID(token, userUUID, startPaging, countPaging, stateFilter);
    }

    @GetMapping(path = "/campaigns/sorted-by/create-time")
    public ResponseEntity<GetCampaignsSortedByCreate.Response> getCampaignsSortedByCreate(@RequestHeader(value = "Authorization", required = false) String token,
                                                                                          @RequestParam(value = "start", required = false) Integer startPaging,
                                                                                          @RequestParam(value = "count", required = false) Integer countPaging,
                                                                                          @RequestParam(value = "state", required = false) String stateFilter,
                                                                                          @RequestParam(value = "tag", required = false) String tagFilter) {
        return campaignHandler.getCampaignsSortedByCreate(token, startPaging, countPaging, stateFilter, tagFilter);
    }

    @GetMapping(path = "/campaigns/sorted-by/famous")
    public ResponseEntity<GetCampaignsSortedByFamous.Response> getCampaignsSortedByFamous(@RequestHeader(value = "Authorization", required = false) String token,
                                                                                          @RequestParam(value = "start", required = false) Integer startPaging,
                                                                                          @RequestParam(value = "count", required = false) Integer countPaging,
                                                                                          @RequestParam(value = "state", required = false) String stateFilter,
                                                                                          @RequestParam(value = "tag", required = false) String tagFilter) {
        return campaignHandler.getCampaignsSortedByFamous(token, startPaging, countPaging, stateFilter, tagFilter);
    }

    @GetMapping(path = "/campaigns/sorted-by/random")
    public ResponseEntity<GetCampaignsSortedByRandom.Response> getCampaignsSortedByRandom(@RequestHeader(value = "Authorization", required = false) String token,
                                                                                          @RequestParam(value = "start", required = false) Integer startPaging,
                                                                                          @RequestParam(value = "count", required = false) Integer countPaging,
                                                                                          @RequestParam(value = "state", required = false) String stateFilter,
                                                                                          @RequestParam(value = "tag", required = false) String tagFilter) {
        return campaignHandler.getCampaignsSortedByRandom(token, startPaging, countPaging, stateFilter, tagFilter);
    }

    @GetMapping(path = "/campaigns/uuid/{campaign_uuid}")
    public ResponseEntity<GetCampaignWithUUID.Response> getCampaignWithUUID(@RequestHeader(value = "Authorization", required = false) String token,
                                                                            @PathVariable("campaign_uuid") String campaignUUID) {
        return campaignHandler.getCampaignWithUUID(token, campaignUUID);
    }

    @PostMapping(path = "/campaigns/with-uuids", consumes = {"application/json"})
    public ResponseEntity<GetCampaignsWithUUIDs.Response> getCampaignsWithUUIDs(@Valid @RequestBody GetCampaignsWithUUIDs.Request req, BindingResult bindingResult,
                                                                                @RequestHeader(value = "Authorization", required = false) String token) {
        return campaignHandler.getCampaignsWithUUIDs(req, bindingResult, token);
    }

    @PostMapping(path = "/campaigns/uuid/{campaign_uuid}/actions/{action}")
    public ResponseEntity<TakeActionInCampaign.Response> takeActionInCampaign(@RequestHeader(value = "Authorization", required = false) String token,
                                                                              @PathVariable("campaign_uuid") String campaignUUID,
                                                                              @PathVariable("action") String action) {
        return campaignHandler.takeActionInCampaign(token, campaignUUID, action);
    }

    @PostMapping(path = "/report")
    public ResponseEntity<ReportCampaign.Response> reportCampaign(@Valid @RequestBody ReportCampaign.Request req, BindingResult bindingResult,
                                                                  @RequestHeader(value = "Authorization", required = false) String token) {
        return campaignHandler.reportCampaign(req, bindingResult, token);
    }

    @GetMapping(path = "/reports/sorted-by/create-time")
    public ResponseEntity<GetCampaignReports.Response> getCampaignReports(@RequestHeader(value = "Authorization", required = false) String token,
                                                                          @RequestParam(value = "start", required = false) Integer startPaging,
                                                                          @RequestParam(value = "count", required = false) Integer countPaging,
                                                                          @RequestParam(value = "state", required = false) String stateFilter) {
        return campaignHandler.getCampaignReports(token, startPaging, countPaging, stateFilter);
    }

    @PostMapping(path = "/reports/uuid/{report_uuid}/actions/{action}")
    public ResponseEntity<TakeActionInReport.Response> takeActionInReport(@RequestHeader(value = "Authorization", required = false) String token,
                                                                          @PathVariable("report_uuid") String reportUUID,
                                                                          @PathVariable("action") String action) {
        return campaignHandler.takeActionInReport(token, reportUUID, action);
    }

    @PostMapping(path = "/participations", consumes = {"multipart/form-data"})
    public ResponseEntity<CreateNewParticipation.Response> createNewParticipation(@Valid @ModelAttribute CreateNewParticipation.Request req, BindingResult bindingResult,
                                                                                  @RequestHeader(value = "Authorization", required = false) String token) throws IOException {
        return campaignHandler.createNewParticipation(req, bindingResult, token);
    }

    @GetMapping(path = "/campaigns/sorted-by/pending-participation")
    public ResponseEntity<GetCampaignsSortedByParticipation.Response> getCampaignsSortedByParticipation(@RequestHeader(value = "Authorization", required = false) String token,
                                                                                                        @RequestParam(value = "start", required = false) Integer startPaging,
                                                                                                        @RequestParam(value = "count", required = false) Integer countPaging) {
        return campaignHandler.getCampaignsSortedByParticipation(token, startPaging, countPaging);
    }

    @GetMapping(path = "/campaigns/uuid/{campaign_uuid}/participations")
    public ResponseEntity<GetParticipationsWithUUID.Response> getParticipationsWithUUID(@RequestHeader(value = "Authorization", required = false) String token,
                                                                          @PathVariable("campaign_uuid") String campaignUUID,
                                                                          @RequestParam(value = "start", required = false) Integer startPaging,
                                                                          @RequestParam(value = "count", required = false) Integer countPaging,
                                                                          @RequestParam(value = "state", required = false) String stateFilter) {
        return campaignHandler.getParticipationsSortedByCreate(token, campaignUUID, startPaging, countPaging, stateFilter);
    }

    @GetMapping(path = "/participations/uuid/{participation_uuid}")
    public ResponseEntity<GetParticipationWithUUID.Response> getParticipationWithUUID(@RequestHeader(value = "Authorization", required = false) String token,
                                                                                      @PathVariable("participation_uuid") String participationUUID) {
        return campaignHandler.getParticipationWithUUID(token, participationUUID);
    }

    @PostMapping(path = "/participations/uuid/{participation_uuid}/actions/{action}")
    public ResponseEntity<TakeActionInParticipation.Response> takeActionInParticipation(@RequestHeader(value = "Authorization", required = false) String token,
                                                                          @PathVariable("participation_uuid") String participationUUID,
                                                                          @PathVariable("action") String action) {
        return campaignHandler.takeActionInParticipation(token, participationUUID, action);
    }

    @GetMapping(path = "/users/uuid/{user_uuid}/participations")
    public ResponseEntity<GetParticipationsWithUserUUID.Response> getParticipationsWithUserUUID(@RequestHeader(value = "Authorization", required = false) String token,
                                                                                      @PathVariable("user_uuid") String userUUID,
                                                                                      @RequestParam(value = "start", required = false) Integer startPaging,
                                                                                      @RequestParam(value = "count", required = false) Integer countPaging,
                                                                                      @RequestParam(value = "state", required = false) String stateFilter) {
        return campaignHandler.getParticipationsWithUserUUID(token, userUUID, startPaging, countPaging, stateFilter);
    }
}
