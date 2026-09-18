package com.salaryneeds.service;

import com.salaryneeds.entity.Job;
import com.salaryneeds.entity.JobApplication;
import com.salaryneeds.repository.JobApplicationRepository;
import com.salaryneeds.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JobPortalService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getJobFeed(String workerId, String tab, String category) {
        if (workerId == null || workerId.isBlank()) workerId = "w-default";

        List<Job> allJobs = jobRepository.findAll();
        if (allJobs.isEmpty()) {
            allJobs = List.of(seedDemoJob("job-101"));
        }

        Set<String> appliedJobIds = new HashSet<>();
        List<JobApplication> apps = jobApplicationRepository.findByWorkerId(workerId);
        for (JobApplication app : apps) {
            appliedJobIds.add(app.getJobId());
        }

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Job job : allJobs) {
            boolean isApplied = appliedJobIds.contains(job.getId());
            if ("APPLIED".equalsIgnoreCase(tab) && !isApplied) {
                continue;
            }
            if (category != null && !category.isBlank()) {
                if (job.getCategory() == null || !job.getCategory().toLowerCase().contains(category.toLowerCase().trim())) {
                    continue;
                }
            }

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", job.getId());
            map.put("title", job.getTitle());
            map.put("company", job.getCompany());
            map.put("category", job.getCategory());
            map.put("location", job.getLocation());
            map.put("salaryRange", job.getSalaryRange());
            map.put("minSalary", job.getMinSalary());
            map.put("maxSalary", job.getMaxSalary());
            map.put("jobType", job.getJobType());
            map.put("experience", job.getExperience());
            map.put("status", job.getStatus());
            map.put("vacancies", job.getVacancies());
            map.put("isApplied", isApplied);
            map.put("benefits", List.of(
                    Map.of("icon", "bus", "label", "Free Transport"),
                    Map.of("icon", "utensils", "label", "Free Lunch")
            ));
            map.put("contactPerson", job.getContactPerson() != null ? job.getContactPerson() : "Suresh Reddy (HR Manager)");
            map.put("contactNumber", job.getContactNumber() != null ? job.getContactNumber() : "+91 98490 22110");
            resultList.add(map);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("jobs", resultList);
        return response;
    }

    @Transactional
    public Map<String, Object> applyJob(String jobId, String workerId) {
        if (workerId == null || workerId.isBlank()) workerId = "w-default";

        Job job = jobRepository.findById(jobId).orElseGet(() -> seedDemoJob(jobId));

        if (!jobApplicationRepository.existsByJobIdAndWorkerId(jobId, workerId)) {
            JobApplication app = JobApplication.builder()
                    .id("app-" + UUID.randomUUID().toString().substring(0, 8))
                    .jobId(jobId)
                    .workerId(workerId)
                    .status("APPLIED")
                    .build();
            jobApplicationRepository.save(app);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Application submitted to " + job.getCompany() + " HR");
        response.put("applicationId", "app-" + Math.abs(jobId.hashCode() ^ workerId.hashCode()));
        return response;
    }

    private Job seedDemoJob(String id) {
        return jobRepository.findById(id).orElseGet(() -> {
            Job j = Job.builder()
                    .id(id)
                    .title("Production Helper")
                    .company("Sri Sai Motors Pvt Ltd")
                    .category("Manufacturing")
                    .location("Hyderabad, Telangana")
                    .salaryRange("₹15,000 - ₹18,000 / month")
                    .minSalary(BigDecimal.valueOf(15000))
                    .maxSalary(BigDecimal.valueOf(18000))
                    .jobType("Full Time")
                    .experience("0-2 Years")
                    .status("Open")
                    .vacancies(5)
                    .description("Assembly line helper for automotive parts manufacturing.")
                    .contactPerson("Suresh Reddy (HR Manager)")
                    .contactNumber("+91 98490 22110")
                    .build();
            return jobRepository.save(j);
        });
    }
}
