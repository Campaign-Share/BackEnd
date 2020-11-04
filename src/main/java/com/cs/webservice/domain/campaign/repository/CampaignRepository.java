package com.cs.webservice.domain.campaign.repository;

import com.cs.webservice.domain.campaign.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, String> {
}
