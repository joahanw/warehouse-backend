package com.johanwork.warehouse.common.config;

import com.johanwork.warehouse.common.config.configProps.AuthType;
import com.johanwork.warehouse.common.config.configProps.OciStorageProperties;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class OciObjectStorageConfig {

    private final OciStorageProperties props;

    @Bean
    public BasicAuthenticationDetailsProvider authenticationDetailsProvider() throws IOException{
        if(props.authType() == AuthType.INSTANCE_PRINCIPAL){
            return InstancePrincipalsAuthenticationDetailsProvider.builder().build();
        }
        ConfigFileReader.ConfigFile configFile =
                ConfigFileReader.parse(props.path(), props.profile());
        return new ConfigFileAuthenticationDetailsProvider(configFile);
    }

    @Bean(destroyMethod = "close")
    public ObjectStorageClient objectStorageClient(BasicAuthenticationDetailsProvider provider) throws Exception {
        ObjectStorageClient client = ObjectStorageClient.builder().build(provider);
        client.setRegion(Region.fromRegionId(props.region()));
        return client;
    }

}
