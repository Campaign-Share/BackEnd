package com.cs.webservice.dto.campaign;

import com.cs.webservice.domain.campaign.Campaign;
import com.cs.webservice.dto.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.util.List;

public class GetAllCampaignList {
    public static class Campaign
    

    @Setter
    @NoArgsConstructor
    public static class Response extends BaseResponse {
        @JsonProperty("campaigns")
        List<Campaign> campaigns;
    }
}
