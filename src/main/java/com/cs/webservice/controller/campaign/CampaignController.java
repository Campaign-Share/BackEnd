package com.cs.webservice.controller.campaign;

import com.cs.webservice.dto.campaign.CreateNewCampaign;
import com.cs.webservice.dto.campaign.GetCampaignsWithUserUUID;
import com.cs.webservice.handler.campaign.CampaignHandlerImpl;
import lombok.RequiredArgsConstructor;
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
    public CreateNewCampaign.Response createNewCampaign(@Valid @ModelAttribute CreateNewCampaign.Request req, BindingResult bindingResult,
                                                        @RequestHeader(value = "Authorization", required = false) String token) throws IOException {
        return campaignHandler.createNewCampaign(req, bindingResult, token);
    }

    @GetMapping(path = "/users/uuid/{user_uuid}/campaigns")
    public GetCampaignsWithUserUUID.Response getCampaignsWithUserUUID(
            @RequestHeader(value = "Authorization", required = false) String token, @PathVariable("user_uuid") String userUUID) {
        return campaignHandler.getCampaignsWithUserUUID(token, userUUID);
    }
}
