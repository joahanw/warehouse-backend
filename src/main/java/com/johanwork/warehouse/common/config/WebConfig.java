package com.johanwork.warehouse.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure API versioning
     * @param configurer
     */
    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer.useMediaTypeParameter(
                MediaType.parseMediaType("application/vnd.johanwork+json"),"v")
                .addSupportedVersions("1.0","2.0")
                .setDefaultVersion("1.0");
    }

    /**
     * Configure path matching
     * @param configurer
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api", controllerType -> true);
    }

}
