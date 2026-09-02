package com.salaryneeds.service;

import com.salaryneeds.dto.AdminRequestDTO;
import com.salaryneeds.dto.AdminResponseDTO;
import com.salaryneeds.entity.Admin;
import com.salaryneeds.exception.AdminNotFoundException;
import com.salaryneeds.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AdminResponseDTO createAdmin(AdminRequestDTO adminRequestDTO) {
        Admin admin = Admin.builder()
                .name(adminRequestDTO.getUsername())
                .email(adminRequestDTO.getMail())
                .passwordHash(passwordEncoder.encode(adminRequestDTO.getPassword()))
                .build();

        Admin savedAdmin = adminRepository.save(admin);
        return mapToResponse(savedAdmin);
    }

    public AdminResponseDTO getAdminById(UUID id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("Admin not found with id: " + id));
        return mapToResponse(admin);
    }

    public List<AdminResponseDTO> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AdminResponseDTO updateAdmin(UUID id, AdminRequestDTO adminRequestDTO) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("Admin not found with id: " + id));

        admin.setName(adminRequestDTO.getUsername());
        admin.setEmail(adminRequestDTO.getMail());

        if (adminRequestDTO.getPassword() != null && !adminRequestDTO.getPassword().isEmpty()) {
            admin.setPasswordHash(passwordEncoder.encode(adminRequestDTO.getPassword()));
        }

        Admin updatedAdmin = adminRepository.save(admin);
        return mapToResponse(updatedAdmin);
    }

    public void deleteAdmin(UUID id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("Admin not found with id: " + id));
        adminRepository.delete(admin);
    }

    private AdminResponseDTO mapToResponse(Admin admin) {
        return AdminResponseDTO.builder()
                .id(admin.getId())
                .username(admin.getName())
                .mail(admin.getEmail())
                .build();
    }
}
