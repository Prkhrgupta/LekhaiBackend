package in.lekhai.core.controller;

import in.lekhai.common.Result;
import in.lekhai.core.model.menu.MenuResponse;
import in.lekhai.core.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(
            MenuService menuService
    ) {
        this.menuService = menuService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Result<MenuResponse>> getMenu() {
        MenuResponse menuResponse = menuService.generateMenu();
        return ResponseEntity.ok(Result.success(menuResponse));
    }
}
