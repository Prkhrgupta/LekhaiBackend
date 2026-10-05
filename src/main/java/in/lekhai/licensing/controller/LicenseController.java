package in.lekhai.licensing.controller;

import in.lekhai.licensing.dto.ActivateLicenseRequest;
import in.lekhai.licensing.dto.LicenseStatusResponse;
import in.lekhai.licensing.service.LicenseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/license")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping("/status")
    public ResponseEntity<LicenseStatusResponse> getLicenseStatus() {
        return ResponseEntity.ok(licenseService.getLicenseStatus());
    }

    @PostMapping("/activate")
    public ResponseEntity<LicenseStatusResponse> activateLicense(@Valid @RequestBody ActivateLicenseRequest request) {
        LicenseStatusResponse response = licenseService.activateLicense(request);
        return ResponseEntity.ok(response);
    }
}
