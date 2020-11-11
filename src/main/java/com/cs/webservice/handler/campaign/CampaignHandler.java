package com.cs.webservice.handler.campaign;

import com.cs.webservice.dto.campaign.CreateNewCampaign;
import com.cs.webservice.dto.campaign.GetCampaignsSortedByCreate;
import com.cs.webservice.dto.campaign.GetCampaignsSortedByFamous;
import com.cs.webservice.dto.campaign.GetCampaignsWithUserUUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.io.IOException;

public interface CampaignHandler {
    ResponseEntity<CreateNewCampaign.Response> createNewCampaign(CreateNewCampaign.Request req, BindingResult bindingResult, String token) throws IOException;
    ResponseEntity<GetCampaignsWithUserUUID.Response> getCampaignsWithUserUUID(String token, String userUUID);
    ResponseEntity<GetCampaignsSortedByCreate.Response> getCampaignsSortedByCreate(String token, Integer startPaging, Integer countPaging,
                                                                                   String statusFilter, String tagFilter);
    ResponseEntity<GetCampaignsSortedByFamous.Response> getCampaignsSortedByFamous(String token, Integer startPaging, Integer countPaging,
                                                                                   String statusFilter, String tagFilter);
}
