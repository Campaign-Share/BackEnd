package com.cs.webservice.domain.campaign.repository;

import com.cs.webservice.domain.campaign.CampaignParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignParticipationRepository extends JpaRepository<CampaignParticipation, String> {
}
