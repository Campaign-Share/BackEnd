package com.cs.webservice.domain.campaign.repository;

import com.cs.webservice.domain.campaign.CampaignVote;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.Optional;

public interface CampaignVoteRepository extends JpaRepository<CampaignVote, Long> {
    Optional<CampaignVote> findByVoterUUIDAndCampaignUUID(String voterUUID, String campaignUUID);
    Optional<CampaignVote> findByVoterUUIDAndCampaignUUIDAndAgree(String voterUUID, String campaignUUID, boolean agree);
    @Transactional
    void deleteByVoterUUIDAndCampaignUUID(String voterUUID, String campaignUUID);
}
