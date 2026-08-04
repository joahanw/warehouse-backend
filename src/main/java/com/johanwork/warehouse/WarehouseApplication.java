package com.johanwork.warehouse;

import com.johanwork.warehouse.common.config.configProps.*;
import com.johanwork.warehouse.common.config.InfisicalEnvironmentInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties(value = {MidtransProperties.class, MailProperties.class, TelegramBotProperties.class, OciStorageProperties.class, CorsProperties.class, WhatsAppProperties.class, BcaQrisProperties.class, WahaAppProperties.class})
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableScheduling
public class WarehouseApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(WarehouseApplication.class)
				.initializers(new InfisicalEnvironmentInitializer())
						.run(args);
	}

}
