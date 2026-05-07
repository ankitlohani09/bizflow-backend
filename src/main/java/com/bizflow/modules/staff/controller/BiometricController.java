package com.bizflow.modules.staff.controller;

import com.bizflow.common.ApiResponse;
import com.bizflow.modules.staff.entity.Staff;
import com.bizflow.modules.staff.entity.StaffBiometric;
import com.bizflow.modules.staff.repository.StaffBiometricRepository;
import com.bizflow.modules.staff.repository.StaffRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/public/biometric")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BiometricController {

    private final StaffBiometricRepository biometricRepository;
    private final StaffRepository staffRepository;

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody RegistrationRequest request) {
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        Optional<StaffBiometric> existing = biometricRepository.findByStaffId(staff.getId());
        StaffBiometric biometric = existing.orElse(new StaffBiometric());

        biometric.setStaff(staff);
        biometric.setTenantId(staff.getTenantId());
        biometric.setCredentialId(request.getCredentialId());
        biometric.setPublicKey(request.getPublicKey());

        biometricRepository.save(biometric);
        return ApiResponse.success("Biometric registered successfully", null);
    }

    @GetMapping("/check/{staffId}")
    public ApiResponse<Map<String, Object>> check(@PathVariable Long staffId) {
        Optional<StaffBiometric> biometric = biometricRepository.findByStaffId(staffId);
        if (biometric.isPresent()) {
            return ApiResponse.success("Credential found",
                    Map.of("hasBiometric", true, "credentialId", biometric.get().getCredentialId()));
        }
        return ApiResponse.success("No biometric found", Map.of("hasBiometric", false));
    }

    @Data
    public static class RegistrationRequest {
        private Long staffId;
        private String credentialId;
        private String publicKey;
    }
}
