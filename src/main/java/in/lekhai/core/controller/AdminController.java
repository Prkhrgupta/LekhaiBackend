package in.lekhai.core.controller;

import in.lekhai.common.Result;
import in.lekhai.core.model.menu.MenuResponse;
import in.lekhai.core.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(
            AdminService adminService
    ) {
        this.adminService = adminService;
    }

    @GetMapping("/fetch-ui-json")
    public ResponseEntity<Result<MenuResponse>> fetchUiJson() {
        MenuResponse menuResponse = adminService.generateUiJson();
        return ResponseEntity.ok(Result.success(menuResponse));
    }
}
