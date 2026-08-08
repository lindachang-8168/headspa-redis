package com.headspa.redis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**好
 * 開發階段先開放全部來源，方便前端直接用瀏覽器打開 html 檔案測試。
 * 之後部署到 AWS、有正式網域了，記得把 "*" 換成實際的前端網址，
 * 例如 "https://headspa.yourdomain.com"，避免任何網站都能呼叫你的 API。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
