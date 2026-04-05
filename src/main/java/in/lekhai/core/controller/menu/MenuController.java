package in.lekhai.core.controller.menu;

import in.lekhai.contract.api.MenuApi;
import in.lekhai.contract.model.MenuResponse;
import in.lekhai.core.service.menu.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MenuController implements MenuApi {

    private final MenuService menuService;

    public MenuController(
            MenuService menuService
    ) {
        this.menuService = menuService;
    }

    @Override
    public ResponseEntity<MenuResponse> getMenu() {
        MenuResponse menuResponse = menuService.generateMenu();
        return ResponseEntity.ok(menuResponse);
    }
}
