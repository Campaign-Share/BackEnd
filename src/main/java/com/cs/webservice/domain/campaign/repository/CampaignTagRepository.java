package com.cs.webservice.domain.campaign.repository;

import com.cs.webservice.domain.campaign.CampaignTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignTagRepository extends JpaRepository<CampaignTag, Long> {
}
