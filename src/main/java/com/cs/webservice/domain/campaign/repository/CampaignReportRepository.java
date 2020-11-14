package com.cs.webservice.domain.campaign.repository;

import com.cs.webservice.domain.campaign.CampaignReport;
import com.cs.webservice.domain.campaign.CampaignVote;
import com.cs.webservice.utils.Random;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CampaignReportRepository extends JpaRepository<CampaignReport, String> {
    Optional<CampaignReport> findByReporterUUIDAndTargetUUID(String reporterUUID, String targetUUID);

    @Query(value = "SELECT * FROM campaign_reports WHERE handled = ?1 AND sanctioned = ?2 ORDER BY created_at DESC LIMIT ?4 OFFSET ?3", nativeQuery = true)
    List<CampaignReport> findAllByHandledAndSanctionedWithPagingSortedByCreatedAt(boolean handled, boolean sanctioned, Integer start, Integer count);

    default String getAvailableUUID() {
        while (true) {
            String reportUUID = "report-" + Random.generateNumberString(12);
            if (this.findById(reportUUID).isEmpty()) {
                return reportUUID;
            }
        }
    }
}
