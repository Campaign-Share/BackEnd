package com.cs.webservice.domain.campaign.repository;

import com.cs.webservice.domain.campaign.CampaignTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignTagRepository extends JpaRepository<CampaignTag, Long> {
    List<CampaignTag> findAllByCampaignUUID(String campaignUUID);
}
