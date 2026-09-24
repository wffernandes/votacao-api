package br.com.sicredi.votacao_api.integracao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class IndiceVotoIntegracaoTest extends AbstractIntegracaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveCriarIndiceParaApuracaoDeVotos() {

        String definicao = jdbcTemplate.queryForObject(
                """
                SELECT indexdef
                FROM pg_indexes
                WHERE schemaname = 'public'
                  AND tablename = 'voto'
                  AND indexname = 'idx_voto_sessao_opcao'
                """,
                String.class
        );

        assertThat(definicao)
                .isNotNull()
                .contains("sessao_id")
                .contains("opcao");
    }
}
