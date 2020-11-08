package com.cs.webservice.domain.campaign.repository;

import com.cs.webservice.domain.campaign.CampaignVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignVoteRepository extends JpaRepository<CampaignVote, Long> {
}
