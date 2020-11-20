package com.cs.webservice.domain.campaign.repository;

import com.cs.webservice.domain.auth.UserAuth;
import com.cs.webservice.domain.campaign.Campaign;
import com.cs.webservice.utils.Random;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, String> {
    List<Campaign> findAllByUserUUIDAndEndDateGreaterThanEqual(String userUUID, LocalDate date);
    Optional<Campaign> findByUuid(String campaignUUID);

    @Query(value = "SELECT * FROM campaigns WHERE uuid IN (SELECT DISTINCT campaign_uuid FROM campaign_tags WHERE tag=?1) " +
            "AND status=?2 AND sanctioned=false AND CURRENT_DATE() <= CAST(end_date AS DATETIME) ORDER BY created_at DESC LIMIT ?4 OFFSET ?3", nativeQuery = true)
    List<Campaign> findAllByTagAndStatusWithPagingSortedByCreatedAt(String tag, Integer status, Integer start, Integer count);
    @Query(value = "SELECT * FROM campaigns WHERE status=?1 AND sanctioned=false AND CURRENT_DATE() <= CAST(end_date AS DATETIME) ORDER BY created_at DESC LIMIT ?3 OFFSET ?2", nativeQuery = true)
    List<Campaign> findAllByStatusWithPagingSortedByCreatedAt(Integer status, Integer start, Integer count);
    @Query(value = "SELECT * FROM campaigns WHERE uuid IN (SELECT DISTINCT campaign_uuid FROM campaign_tags WHERE tag=?1) " +
            "AND sanctioned=false AND CURRENT_DATE() <= CAST(end_date AS DATETIME) ORDER BY created_at DESC LIMIT ?3 OFFSET ?2", nativeQuery = true)
    List<Campaign> findAllByTagWithPagingSortedByCreatedAt(String tag, Integer start, Integer count);
    @Query(value = "SELECT * FROM campaigns WHERE sanctioned=false AND CURRENT_DATE() <= CAST(end_date AS DATETIME) ORDER BY created_at DESC LIMIT ?2 OFFSET ?1", nativeQuery = true)
    List<Campaign> findAllWithPagingSortedByCreatedAt(Integer start, Integer count);


    @Query(value = "SELECT * FROM campaigns WHERE uuid IN (SELECT DISTINCT campaign_uuid FROM campaign_tags WHERE tag=?1) " +
            "AND status=?2 AND sanctioned=false AND CURRENT_DATE() <= CAST(end_date AS DATETIME) ORDER BY agree_number + disagree_number DESC LIMIT ?4 OFFSET ?3", nativeQuery = true)
    List<Campaign> findAllByTagAndStatusWithPagingSortedByFamous(String tag, Integer status, Integer start, Integer count);
    @Query(value = "SELECT * FROM campaigns WHERE status=?1 AND sanctioned=false AND CURRENT_DATE() <= CAST(end_date AS DATETIME) ORDER BY agree_number + disagree_number DESC LIMIT ?3 OFFSET ?2", nativeQuery = true)
    List<Campaign> findAllByStatusWithPagingSortedByFamous(Integer status, Integer start, Integer count);
    @Query(value = "SELECT * FROM campaigns WHERE uuid IN (SELECT DISTINCT campaign_uuid FROM campaign_tags WHERE tag=?1) " +
            "AND sanctioned=false AND CURRENT_DATE() <= CAST(end_date AS DATETIME) ORDER BY agree_number + disagree_number DESC LIMIT ?3 OFFSET ?2", nativeQuery = true)
    List<Campaign> findAllByTagWithPagingSortedByFamous(String tag, Integer start, Integer count);
    @Query(value = "SELECT * FROM campaigns WHERE sanctioned=false AND CURRENT_DATE() <= CAST(end_date AS DATETIME) ORDER BY agree_number + disagree_number DESC LIMIT ?2 OFFSET ?1", nativeQuery = true)
    List<Campaign> findAllWithPagingSortedByFamous(Integer start, Integer count);

    @Query(value = "SELECT * FROM campaigns WHERE uuid IN (SELECT DISTINCT campaign_uuid FROM campaign_tags WHERE tag=?1) " +
            "AND status=?2 AND sanctioned=false AND CURRENT_DATE() <= CAST(end_date AS DATETIME) ORDER BY RAND() DESC LIMIT ?4 OFFSET ?3", nativeQuery = true)
    List<Campaign> findAllByTagAndStatusWithPagingSortedByRandom(String tag, Integer status, Integer start, Integer count);
    @Query(value = "SELECT * FROM campaigns WHERE status=?1 AND sanctioned=false AND CURRENT_DATE() <= CAST(end_date AS DATETIME) ORDER BY RAND() DESC LIMIT ?3 OFFSET ?2", nativeQuery = true)
    List<Campaign> findAllByStatusWithPagingSortedByRandom(Integer status, Integer start, Integer count);
    @Query(value = "SELECT * FROM campaigns WHERE uuid IN (SELECT DISTINCT campaign_uuid FROM campaign_tags WHERE tag=?1) " +
            "AND sanctioned=false AND CURRENT_DATE() <= CAST(end_date AS DATETIME) ORDER BY RAND() DESC LIMIT ?3 OFFSET ?2", nativeQuery = true)
    List<Campaign> findAllByTagWithPagingSortedByRandom(String tag, Integer start, Integer count);
    @Query(value = "SELECT * FROM campaigns WHERE sanctioned=false AND CURRENT_DATE() <= CAST(end_date AS DATETIME) ORDER BY RAND() DESC LIMIT ?2 OFFSET ?1", nativeQuery = true)
    List<Campaign> findAllWithPagingSortedByRandom(Integer start, Integer count);

    @Query(value = "SELECT * FROM campaigns WHERE user_uuid = ?1 AND sanctioned=false ORDER BY created_at DESC LIMIT ?3 OFFSET ?2", nativeQuery = true)
    List<Campaign> findAllByUserUUIDWithPagingSortedByCreatedAt(String userUUID, Integer start, Integer count);
    @Query(value = "SELECT * FROM campaigns WHERE user_uuid = ?1 AND status=?2 AND sanctioned=false ORDER BY created_at DESC LIMIT ?4 OFFSET ?3", nativeQuery = true)
    List<Campaign> findAllByUserUUIDAndStatusWithPagingSortedByCreatedAt(String userUUID, Integer status, Integer start, Integer count);

    @Query(value = "SELECT * FROM campaigns WHERE uuid IN (?1) AND status=1 ORDER BY FIELD(uuid, ?1)", nativeQuery = true)
    List<Campaign> findAllByUUIDSortedByUUID(List<String> campaignUUIDs);

    @Query(value = "SELECT * FROM campaigns WHERE uuid IN (?1) AND status=1 AND CURRENT_DATE() <= CAST(end_date AS DATETIME) ORDER BY FIELD(uuid, ?1)", nativeQuery = true)
    List<Campaign> findAllByUUIDAndEndDateGreaterThanNowSortedByUUID(List<String> campaignUUIDs);

    default String getAvailableUUID() {
        while (true) {
            String campaignUUID = "campaign-" + Random.generateNumberString(12);
            if (this.findById(campaignUUID).isEmpty()) {
                return campaignUUID;
            }
        }
    }
}
