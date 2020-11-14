package com.cs.webservice.domain.campaign.repository;

import com.cs.webservice.domain.campaign.CampaignReport;
import com.cs.webservice.domain.campaign.CampaignVote;
import com.cs.webservice.utils.Random;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CampaignReportRepository extends JpaRepository<CampaignReport, String> {
    Optional<CampaignReport> findByReporterUUIDAndTargetUUID(String reporterUUID, String targetUUID);

    default String getAvailableUUID() {
        while (true) {
            String reportUUID = "report-" + Random.generateNumberString(12);
            if (this.findById(reportUUID).isEmpty()) {
                return reportUUID;
            }
        }
    }
}
