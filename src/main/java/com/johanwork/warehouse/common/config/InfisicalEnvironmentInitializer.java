package com.johanwork.warehouse.common.config;

import com.infisical.sdk.InfisicalSdk;
import com.infisical.sdk.models.Secret;
import com.infisical.sdk.util.InfisicalException;
import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class InfisicalEnvironmentInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment env = context.getEnvironment();

        String clientId = env.getProperty("INFISICAL_CLIENT_ID");
        String clientSecret = env.getProperty("INFISICAL_CLIENT_SECRET");
        String projectId = env.getProperty("INFISICAL_PROJECT_ID");
        String environment = env.getProperty("INFISICAL_ENVIRONMENT", "dev");

        if (null == clientId || null == clientSecret || null == projectId){
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE,
                   AppConstant.Error.TITLE_INFISICAL_UNAVAILABLE,
                    AppConstant.Error.MESSAGE_INFISICAL_UNAVAILABLE);
        }

        InfisicalSdk sdk = new InfisicalSdk(
                new com.infisical.sdk.config.SdkConfig.Builder()
                        .withSiteUrl("https://app.infisical.com")
                        .build()
        );
        try {
            sdk.Auth().UniversalAuthLogin(clientId, clientSecret);
        } catch (InfisicalException e) {
            log.error("Failed to initialize Infisical environment", e);
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE,
                    AppConstant.Error.TITLE_INFISICAL_UNAVAILABLE,
                    AppConstant.Error.MESSAGE_INFISICAL_UNAVAILABLE);
        }

        try {
            List<Secret> secrets = sdk.Secrets().ListSecrets(
                    projectId,
                    environment,
                    "/",     // secretPath, "/" = root folder
                    false,   // expandSecretReferences
                    false,   // recursive
                    false,   // includeImports
                    false    // setSecretsOnSystemProperties — kita handle manual lewat Spring Environment
            );

            Map<String, Object> secretMap = new HashMap<>();
            for (Secret secret : secrets) {
                secretMap.put(secret.getSecretKey(), secret.getSecretValue());
            }

            env.getPropertySources().addFirst(
                    new MapPropertySource("infisicalSecrets", secretMap)
            );

            log.info("Successfully loaded " + secretMap.size() + " secrets from Infisical");
        } catch (InfisicalException e) {
            log.error("Failed to list secrets from Infisical", e);
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE,
                    AppConstant.Error.TITLE_INFISICAL_UNAVAILABLE,
                    AppConstant.Error.MESSAGE_INFISICAL_UNAVAILABLE);
        }


    }

}
