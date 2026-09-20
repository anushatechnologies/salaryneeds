package com.salaryneeds.repository;

import com.salaryneeds.entity.WorkerProfile;
import com.salaryneeds.entity.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, UUID>, JpaSpecificationExecutor<WorkerProfile> {

    Optional<WorkerProfile> findByPhone(String phone);

    boolean existsByPhone(String phone);

    Optional<WorkerProfile> findByEmail(String email);

    boolean existsByEmail(String email);

    List<WorkerProfile> findByDutyOnlineTrue();

    @Query("SELECT w FROM WorkerProfile w WHERE w.dutyOnline = TRUE AND w.category.id = :categoryId")
    List<WorkerProfile> findByDutyOnlineTrueAndCategoryId(@Param("categoryId") UUID categoryId);

    @Query("SELECT w FROM WorkerProfile w WHERE " +
           "(:categoryId IS NULL OR w.category.id = :categoryId) AND " +
           "(:service IS NULL OR LOWER(w.service) LIKE LOWER(CONCAT('%', :service, '%'))) AND " +
           "(:pincode IS NULL OR w.pincode = :pincode) AND " +
           "(:minRating IS NULL OR w.ratingAvg >= :minRating) AND " +
           "(:dutyOnline IS NULL OR w.dutyOnline = :dutyOnline) AND " +
           "(w.accountStatus = com.salaryneeds.entity.enums.AccountStatus.ACTIVE)")
    Page<WorkerProfile> searchWorkers(
            @Param("categoryId") UUID categoryId,
            @Param("service") String service,
            @Param("pincode") String pincode,
            @Param("minRating") BigDecimal minRating,
            @Param("dutyOnline") Boolean dutyOnline,
            Pageable pageable
    );

    @Query("SELECT w FROM WorkerProfile w WHERE " +
           "w.pincode = :pincode AND " +
           "(:categoryId IS NULL OR w.category.id = :categoryId) AND " +
           "w.dutyOnline = TRUE AND w.accountStatus = com.salaryneeds.entity.enums.AccountStatus.ACTIVE " +
           "ORDER BY w.ratingAvg DESC, w.completedJobsCount DESC")
    List<WorkerProfile> findRecommendedWorkers(
            @Param("pincode") String pincode,
            @Param("categoryId") UUID categoryId,
            Pageable pageable
    );

    @Query("SELECT w FROM WorkerProfile w WHERE " +
           "w.verified = TRUE AND w.dutyOnline = TRUE AND w.accountStatus = com.salaryneeds.entity.enums.AccountStatus.ACTIVE AND " +
           "(:categoryId IS NULL OR w.category.id = :categoryId OR " +
           "(:serviceName IS NOT NULL AND (LOWER(w.service) LIKE LOWER(CONCAT('%', :serviceName, '%')) OR " +
           "LOWER(w.skills) LIKE LOWER(CONCAT('%', :serviceName, '%')))))")
    List<WorkerProfile> findEligibleCandidateWorkers(
            @Param("categoryId") UUID categoryId,
            @Param("serviceName") String serviceName
    );

    // Admin service methods
    Page<WorkerProfile> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByAccountStatus(AccountStatus accountStatus);

    long countByVerified(boolean verified);

    Page<WorkerProfile> findAllByVerifiedOrderByCreatedAtDesc(boolean verified, Pageable pageable);
}
