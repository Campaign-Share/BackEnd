package com.cs.webservice.domain.campaign.repository;

import com.cs.webservice.domain.auth.UserAuth;
import com.cs.webservice.domain.campaign.Campaign;
import com.cs.webservice.utils.Random;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, String> {
    List<Campaign> findAllByUserUUIDAndEndDateGreaterThanEqual(String userUUID, LocalDate date);
    List<Campaign> findAllByUserUUID(String userUUID);

    default String getAvailableUUID() {
        while (true) {
            String campaignUUID = "campaign-" + Random.generateNumberString(12);
            if (this.findById(campaignUUID).isEmpty()) {
                return campaignUUID;
            }
        }
    }
}
