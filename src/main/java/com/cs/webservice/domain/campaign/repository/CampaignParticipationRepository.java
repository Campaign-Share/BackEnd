package com.cs.webservice.domain.campaign.repository;

import com.cs.webservice.domain.campaign.Campaign;
import com.cs.webservice.domain.campaign.CampaignParticipation;
import com.cs.webservice.utils.Random;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CampaignParticipationRepository extends JpaRepository<CampaignParticipation, String> {
    Optional<CampaignParticipation> findByParticipantUUIDAndCampaignUUID(String participantUUID, String campaignUUID);

    @Query(value = "SELECT campaign_uuid FROM campaign_participations WHERE state=0 GROUP BY campaign_uuid ORDER BY COUNT(*) DESC LIMIT ?2 OFFSET ?1", nativeQuery = true)
    List<String> findAllWithPagingSortedByTotalPendingNumber(Integer start, Integer count);

    default String getAvailableUUID() {
        while (true) {
            String participationUUID = "participation-" + Random.generateNumberString(12);
            if (this.findById(participationUUID).isEmpty()) {
                return participationUUID;
            }
        }
    }
}
