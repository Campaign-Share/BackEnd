package com.cs.webservice.domain.campaign.repository;

import com.cs.webservice.domain.campaign.CampaignReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignReportRepository extends JpaRepository<CampaignReport, String> {
}
