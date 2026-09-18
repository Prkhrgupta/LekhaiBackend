package in.lekhai.test.controller;

import in.lekhai.common.Result;
import in.lekhai.test.service.TestCleanupService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@ConditionalOnProperty(name = "lekhai.test.endpoints.enabled", havingValue = "true")
public class TestCleanupController {

    private final TestCleanupService testCleanupService;

    public TestCleanupController(TestCleanupService testCleanupService) {
        this.testCleanupService = testCleanupService;
    }

    @DeleteMapping("/cleanup/shop/{shopCode}")
    public ResponseEntity<Result<String>> cleanupShop(@PathVariable Integer shopCode) {
        testCleanupService.cleanupShop(shopCode);
        return ResponseEntity.ok(Result.success("Shop cleaned up successfully: " + shopCode));
    }

    @DeleteMapping("/cleanup/user/{username}")
    public ResponseEntity<Result<String>> cleanupUser(@PathVariable String username) {
        testCleanupService.cleanupUser(username);
        return ResponseEntity.ok(Result.success("User cleaned up successfully: " + username));
    }

    @DeleteMapping("/cleanup/all")
    public ResponseEntity<Result<String>> cleanupAll() {
        testCleanupService.cleanupAllTestData();
        return ResponseEntity.ok(Result.success("All test data cleaned up successfully"));
    }
}
