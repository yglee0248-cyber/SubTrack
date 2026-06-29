package com.subtrack.global.health;

import com.subtrack.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ApiResponse<HealthStatusResponse> health() {
        return ApiResponse.success("OK", new HealthStatusResponse("UP"));
    }

    public static class HealthStatusResponse {

        private final String status;

        public HealthStatusResponse(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }
}
