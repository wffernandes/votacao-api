package br.com.sicredi.votacao_api;

import br.com.sicredi.votacao_api.config.PostgresTestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgresTestcontainersConfiguration.class)
class VotacaoApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
