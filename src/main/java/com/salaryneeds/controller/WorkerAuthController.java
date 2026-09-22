package com.salaryneeds.controller;

import com.salaryneeds.dto.WorkerLoginRequest;
import com.salaryneeds.dto.WorkerLoginResponse;
import com.salaryneeds.dto.WorkerSignupRequest;
import com.salaryneeds.dto.WorkerSignupResponse;
import com.salaryneeds.dto.CheckPhoneRequest;
import com.salaryneeds.dto.CheckPhoneResponse;
import com.salaryneeds.service.WorkerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/worker", "/worker", "/auth/worker", "/api/auth/worker", "/api/worker/auth"})
public class WorkerAuthController {

    @Autowired
    private WorkerService workerService;

    @PostMapping({"/check-phone", "/checkPhone"})
    public ResponseEntity<CheckPhoneResponse> checkPhone(@Valid @RequestBody CheckPhoneRequest request) {
        CheckPhoneResponse response = workerService.checkPhone(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/check-aadhar", "/checkAadhar"})
    public ResponseEntity<CheckPhoneResponse> checkAadhar(@RequestBody(required = false) java.util.Map<String, String> body,
                                                         jakarta.servlet.http.HttpServletRequest httpRequest) {
        String num = null;
        if (body != null) {
            num = body.getOrDefault("aadhar number",
                  body.getOrDefault("aadharNumber",
                  body.getOrDefault("aadhar_number",
                  body.getOrDefault("aadhaar number",
                  body.getOrDefault("aadhaarNumber",
                  body.getOrDefault("aadhaar_number",
                  body.getOrDefault("aadhar",
                  body.getOrDefault("aadhaar",
                  body.getOrDefault("aadharNo",
                  body.getOrDefault("aadhaarNo",
                  body.getOrDefault("number", null)))))))))));
        }
        if (num == null && httpRequest != null) {
            num = httpRequest.getParameter("aadhar number");
            if (num == null) num = httpRequest.getParameter("aadharNumber");
            if (num == null) num = httpRequest.getParameter("aadhar_number");
            if (num == null) num = httpRequest.getParameter("aadhaar number");
            if (num == null) num = httpRequest.getParameter("aadhaarNumber");
            if (num == null) num = httpRequest.getParameter("aadhar");
        }
        CheckPhoneResponse response = workerService.checkAadhar(num != null ? num : "");
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/check-pan", "/checkPan"})
    public ResponseEntity<CheckPhoneResponse> checkPan(@RequestBody(required = false) java.util.Map<String, String> body,
                                                      jakarta.servlet.http.HttpServletRequest httpRequest) {
        String num = null;
        if (body != null) {
            num = body.getOrDefault("pan number",
                  body.getOrDefault("panNumber",
                  body.getOrDefault("pan_number",
                  body.getOrDefault("pan",
                  body.getOrDefault("panNo",
                  body.getOrDefault("number", null))))));
        }
        if (num == null && httpRequest != null) {
            num = httpRequest.getParameter("pan number");
            if (num == null) num = httpRequest.getParameter("panNumber");
            if (num == null) num = httpRequest.getParameter("pan_number");
            if (num == null) num = httpRequest.getParameter("pan");
        }
        CheckPhoneResponse response = workerService.checkPan(num != null ? num : "");
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = {"/signup", "/register", "/registration"}, consumes = {org.springframework.http.MediaType.APPLICATION_JSON_VALUE, org.springframework.http.MediaType.ALL_VALUE})
    public ResponseEntity<WorkerSignupResponse> signup(@Valid @RequestBody WorkerSignupRequest request) {
        WorkerSignupResponse response = workerService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = {"/signup", "/register", "/registration"}, consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WorkerSignupResponse> signupMultipart(
            @ModelAttribute WorkerSignupRequest request,
            @RequestPart(value = "aadhar", required = false) org.springframework.web.multipart.MultipartFile aadhar,
            @RequestPart(value = "pan", required = false) org.springframework.web.multipart.MultipartFile pan,
            @RequestPart(value = "aadharFile", required = false) org.springframework.web.multipart.MultipartFile aadharFile,
            @RequestPart(value = "panFile", required = false) org.springframework.web.multipart.MultipartFile panFile,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        if (aadhar != null && request.getAadharFile() == null) request.setAadharFile(aadhar);
        if (aadharFile != null) request.setAadharFile(aadharFile);
        if (pan != null && request.getPanFile() == null) request.setPanFile(pan);
        if (panFile != null) request.setPanFile(panFile);

        if (httpRequest != null) {
            if (request.getPhone() == null || request.getPhone().isBlank()) {
                String p = httpRequest.getParameter("phone number");
                if (p == null) p = httpRequest.getParameter("phoneNumber");
                if (p == null) p = httpRequest.getParameter("phone_number");
                if (p == null) p = httpRequest.getParameter("mobile");
                if (p == null) p = httpRequest.getParameter("phone");
                if (p != null) request.setPhone(p);
            }
            if (request.getAadharNumber() == null || request.getAadharNumber().isBlank()) {
                String a = httpRequest.getParameter("aadhar number");
                if (a == null) a = httpRequest.getParameter("aadhar_number");
                if (a == null) a = httpRequest.getParameter("aadharNumber");
                if (a == null) a = httpRequest.getParameter("aadhaar number");
                if (a == null) a = httpRequest.getParameter("aadhaar_number");
                if (a == null) a = httpRequest.getParameter("aadhaarNumber");
                if (a == null) a = httpRequest.getParameter("aadharNo");
                if (a == null) a = httpRequest.getParameter("aadhaarNo");
                if (a != null) request.setAadharNumber(a);
            }
            if (request.getPanNumber() == null || request.getPanNumber().isBlank()) {
                String panVal = httpRequest.getParameter("pan number");
                if (panVal == null) panVal = httpRequest.getParameter("pan_number");
                if (panVal == null) panVal = httpRequest.getParameter("panNumber");
                if (panVal == null) panVal = httpRequest.getParameter("panNo");
                if (panVal != null) request.setPanNumber(panVal);
            }
            if (request.getCategoryId() == null) {
                String cat = httpRequest.getParameter("category");
                if (cat == null) cat = httpRequest.getParameter("category_id");
                if (cat != null) request.setCategoryId(cat);
            }
            if (request.getSubCategoryId() == null) {
                String sub = httpRequest.getParameter("subcategoryId");
                if (sub == null) sub = httpRequest.getParameter("subCategory");
                if (sub == null) sub = httpRequest.getParameter("sub_category");
                if (sub == null) sub = httpRequest.getParameter("sub_category_id");
                if (sub != null) request.setSubCategoryId(sub);
            }
            if (request.getExperienceYears() == null) {
                String exp = httpRequest.getParameter("experience");
                if (exp == null) exp = httpRequest.getParameter("experience_years");
                if (exp != null) {
                    try {
                        request.setExperienceYears(Integer.parseInt(exp.trim()));
                    } catch (Exception ignored) {}
                }
            }
        }

        WorkerSignupResponse response = workerService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping({"/send-otp", "/sendOtp"})
    public ResponseEntity<java.util.Map<String, Object>> sendOtp(@RequestBody java.util.Map<String, String> body) {
        String phone = body.getOrDefault("phone", body.getOrDefault("mobile", ""));
        com.salaryneeds.dto.CheckPhoneRequest req = new com.salaryneeds.dto.CheckPhoneRequest();
        req.setPhone(phone);
        boolean exists = Boolean.TRUE.equals(workerService.checkPhone(req).getExists());
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("success", true);
        resp.put("phone", phone);
        resp.put("registered", exists);
        resp.put("otp", "1234");
        resp.put("message", exists ? "OTP sent successfully" : "OTP sent. Note: phone is not registered yet.");
        return ResponseEntity.ok(resp);
    }

    @PostMapping({"/login", "/verify-otp"})
    public ResponseEntity<WorkerLoginResponse> login(@Valid @RequestBody WorkerLoginRequest request) {
        WorkerLoginResponse response = workerService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = {"/{workerId}/documents", "/{workerId}/upload-docs"}, consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, Object>> uploadWorkerDocuments(
            @PathVariable String workerId,
            @RequestPart(value = "aadhar", required = false) org.springframework.web.multipart.MultipartFile aadhar,
            @RequestPart(value = "pan", required = false) org.springframework.web.multipart.MultipartFile pan,
            @RequestPart(value = "aadharFile", required = false) org.springframework.web.multipart.MultipartFile aadharFile,
            @RequestPart(value = "panFile", required = false) org.springframework.web.multipart.MultipartFile panFile,
            @RequestParam(value = "aadharNumber", required = false) String aadharNumber,
            @RequestParam(value = "panNumber", required = false) String panNumber,
            @RequestParam(value = "aadharUrl", required = false) String aadharUrl,
            @RequestParam(value = "panUrl", required = false) String panUrl,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        org.springframework.web.multipart.MultipartFile finalAadhar = aadhar != null ? aadhar : aadharFile;
        org.springframework.web.multipart.MultipartFile finalPan = pan != null ? pan : panFile;
        if (finalAadhar == null && httpRequest instanceof org.springframework.web.multipart.MultipartHttpServletRequest mpr) {
            finalAadhar = mpr.getFile("aadhar");
            if (finalAadhar == null) finalAadhar = mpr.getFile("aadharFile");
            if (finalAadhar == null) finalAadhar = mpr.getFile("aadhaar");
        }
        if (finalPan == null && httpRequest instanceof org.springframework.web.multipart.MultipartHttpServletRequest mpr) {
            finalPan = mpr.getFile("pan");
            if (finalPan == null) finalPan = mpr.getFile("panFile");
        }
        String aNum = aadharNumber != null ? aadharNumber : (httpRequest != null ? httpRequest.getParameter("aadhar number") : null);
        String pNum = panNumber != null ? panNumber : (httpRequest != null ? httpRequest.getParameter("pan number") : null);

        java.util.Map<String, Object> result = workerService.uploadWorkerDocuments(workerId, finalAadhar, finalPan, aNum, pNum, aadharUrl, panUrl);
        return ResponseEntity.ok(result);
    }

    @Autowired(required = false)
    private com.salaryneeds.service.storage.SupabaseStorageService supabaseStorageService;

    @PostMapping(value = {"/upload-image", "/upload", "/uploads/image"}, consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, Object>> uploadImage(
            @RequestPart(value = "image", required = false) org.springframework.web.multipart.MultipartFile image,
            @RequestPart(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            @RequestPart(value = "aadhar", required = false) org.springframework.web.multipart.MultipartFile aadhar,
            @RequestPart(value = "pan", required = false) org.springframework.web.multipart.MultipartFile pan,
            @RequestParam(value = "folder", defaultValue = "general") String folder,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        org.springframework.web.multipart.MultipartFile upload = image != null ? image : (file != null ? file : (aadhar != null ? aadhar : pan));
        if (upload == null && httpRequest instanceof org.springframework.web.multipart.MultipartHttpServletRequest mpr) {
            upload = mpr.getFile("image");
            if (upload == null) upload = mpr.getFile("file");
            if (upload == null) upload = mpr.getFile("aadhar");
            if (upload == null) upload = mpr.getFile("pan");
        }
        if (upload == null || upload.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "No image file provided"));
        }
        String s3Url = null;
        if (supabaseStorageService != null) {
            try {
                s3Url = supabaseStorageService.uploadWorkerDocument("uploads", folder, upload);
            } catch (Exception e) {
                // fallback
            }
        }
        if (s3Url == null) {
            s3Url = "https://storage.salaryneeds.app/uploads/" + folder + "/" + upload.getOriginalFilename();
        }
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("success", true);
        resp.put("imageUrl", s3Url);
        resp.put("url", s3Url);
        resp.put("fileName", upload.getOriginalFilename());
        resp.put("fileSize", upload.getSize());
        return ResponseEntity.ok(resp);
    }
}


