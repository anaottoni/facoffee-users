package facoffe.users.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    // carrega a URL do keycloak
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public Keycloak keycloak() {
        // extrai a URL base tirando o caminho do realm 
        String serverUrl = issuerUri.split("/realms/")[0];

        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master") // O client admin geralmente autentica no realm master
                .clientId("admin-cli") // Client padrão do Keycloak com permissão administrativa
                .username("facoffee") // O KEYCLOAK_ADMIN do seu docker-compose
                .password("facoffee") // O KEYCLOAK_ADMIN_PASSWORD do seu docker-compose
                .build();
    }
}