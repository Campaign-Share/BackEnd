package com.cs.webservice.handler.campaign;

import com.cs.webservice.dto.campaign.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.io.IOException;

public interface CampaignHandler {
    ResponseEntity<CreateNewCampaign.Response> createNewCampaign(CreateNewCampaign.Request req, BindingResult bindingResult, String token) throws IOException;
    ResponseEntity<GetCampaignsWithUserUUID.Response> getCampaignsWithUserUUID(String token, String userUUID,
                                                                               Integer startPaging, Integer countPaging, String stateFilter);
    ResponseEntity<GetCampaignsSortedByCreate.Response> getCampaignsSortedByCreate(String token, Integer startPaging, Integer countPaging,
                                                                                   String stateFilter, String tagFilter);
    ResponseEntity<GetCampaignsSortedByFamous.Response> getCampaignsSortedByFamous(String token, Integer startPaging, Integer countPaging,
                                                                                   String stateFilter, String tagFilter);
    ResponseEntity<GetCampaignsSortedByRandom.Response> getCampaignsSortedByRandom(String token, Integer startPaging, Integer countPaging,
                                                                                   String stateFilter, String tagFilter);
    ResponseEntity<GetCampaignWithUUID.Response> getCampaignWithUUID(String token, String userUUID);
    ResponseEntity<GetCampaignsWithUUIDs.Response> getCampaignsWithUUIDs(GetCampaignsWithUUIDs.Request req, BindingResult bindingResult, String token);
}
