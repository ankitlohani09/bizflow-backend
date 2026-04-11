package com.bizflow.config;

import com.bizflow.common.constant.MessageConstant;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title(MessageConstant.API_DOCUMENTATION)
                        .description("BizFlow - AI Powered Business Operating System").version(MessageConstant.V1))
                .addSecurityItem(new SecurityRequirement().addList(MessageConstant.BEARER_NAME))
                .components(new Components().addSecuritySchemes(MessageConstant.BEARER_NAME,
                        new SecurityScheme().name(MessageConstant.BEARER_NAME).type(SecurityScheme.Type.HTTP)
                                .scheme(MessageConstant.BEARER_NAME).bearerFormat(MessageConstant.JWT)
                                .description(MessageConstant.BEARER_AUTHENTICATION)));
    }
}