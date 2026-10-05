package in.lekhai.licensing.config;

import in.lekhai.licensing.interceptor.LicenseGuardInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LicenseGuardInterceptor licenseGuardInterceptor;

    public WebMvcConfig(LicenseGuardInterceptor licenseGuardInterceptor) {
        this.licenseGuardInterceptor = licenseGuardInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(licenseGuardInterceptor)
                .addPathPatterns("/**");
    }
}
