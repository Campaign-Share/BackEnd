package com.cs.webservice.handler.campaign;

import com.cs.webservice.dto.campaign.CreateNewCampaign;
import com.cs.webservice.dto.campaign.GetCampaignsWithUserUUID;
import org.springframework.validation.BindingResult;

import java.io.IOException;

public interface CampaignHandler {
    CreateNewCampaign.Response createNewCampaign(CreateNewCampaign.Request req, BindingResult bindingResult, String token) throws IOException;
    GetCampaignsWithUserUUID.Response getCampaignsWithUserUUID(String token, String userUUID);
}
