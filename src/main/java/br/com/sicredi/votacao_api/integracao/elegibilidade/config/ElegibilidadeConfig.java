package br.com.sicredi.votacao_api.integracao.elegibilidade.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class ElegibilidadeConfig {

    @Bean
    public RestClient elegibilidadeRestClient(
            @Value("${integracao.elegibilidade.base-url}")
            String baseUrl,
            @Value("${integracao.elegibilidade.timeout-segundos}")
            long timeoutSegundos) {

        Duration timeout = Duration.ofSeconds(timeoutSegundos);

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
