package br.com.sicredi.votacao_api.performance;

import br.com.sicredi.votacao_api.integracao.AbstractIntegracaoTest;
import br.com.sicredi.votacao_api.voto.entity.OpcaoVoto;
import br.com.sicredi.votacao_api.voto.repository.VotoRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ResultadoPerformanceTest extends AbstractIntegracaoTest {

    private static final int TOTAL_VOTOS = 100_000;
    private static final int TOTAL_SIM = 60_000;
    private static final int TOTAL_NAO = 40_000;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private VotoRepository votoRepository;

    @Test
    void deveApurarCemMilVotos() throws Exception {

        Long pautaId = criarPauta("teste de performance");

        Long sessaoId = abrirSessao(pautaId, 5);

        inserirVotosEmLote(sessaoId);

        Instant inicio = Instant.now();

        var contagem = votoRepository.contarVotosPorSessao(
                sessaoId,
                OpcaoVoto.SIM,
                OpcaoVoto.NAO
        );

        Duration duracao = Duration.between(inicio, Instant.now());

        assertThat(contagem.getTotalSim()).isEqualTo((long) TOTAL_SIM);
        assertThat(contagem.getTotalNao()).isEqualTo((long) TOTAL_NAO);

        System.out.printf("Apuração de %,d votos concluída em %d ms%n", TOTAL_VOTOS, duracao.toMillis());
    }

    private void inserirVotosEmLote(Long sessaoId) {

        Instant agora = Instant.now();

        jdbcTemplate.batchUpdate(
                """
                INSERT INTO voto (
                    sessao_id,
                    associado_id,
                    opcao,
                    votado_em
                )
                VALUES (?, ?, ?, ?)
                """,
                new org.springframework.jdbc.core.BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(
                            java.sql.@NonNull PreparedStatement ps,
                            int i
                    ) throws java.sql.SQLException {

                        ps.setLong(1, sessaoId);

                        ps.setString(
                                2,
                                "associado-" + i
                        );

                        ps.setString(
                                3,
                                i < TOTAL_SIM
                                        ? "SIM"
                                        : "NAO"
                        );

                        ps.setTimestamp(
                                4,
                                Timestamp.from(agora)
                        );
                    }

                    @Override
                    public int getBatchSize() {
                        return TOTAL_VOTOS;
                    }
                }
        );
    }



}
