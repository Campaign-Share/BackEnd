package com.cs.webservice.domain.campaign.repository;

import com.cs.webservice.domain.campaign.CampaignParticipation;
import com.cs.webservice.utils.Random;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CampaignParticipationRepository extends JpaRepository<CampaignParticipation, String> {
    Optional<CampaignParticipation> findByParticipantUUIDAndCampaignUUID(String participantUUID, String campaignUUID);

    default String getAvailableUUID() {
        while (true) {
            String participationUUID = "participation-" + Random.generateNumberString(12);
            if (this.findById(participationUUID).isEmpty()) {
                return participationUUID;
            }
        }
    }
}
