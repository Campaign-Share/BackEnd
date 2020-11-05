package com.cs.webservice.domain.campaign.repository;

import com.cs.webservice.domain.campaign.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, String> {
    List<Campaign> findAllByUserUUIDAndEndDateGreaterThanEqual(String userUUID, LocalDate date);
}
