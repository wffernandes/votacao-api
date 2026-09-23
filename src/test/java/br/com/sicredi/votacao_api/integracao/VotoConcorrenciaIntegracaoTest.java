package br.com.sicredi.votacao_api.integracao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class VotoConcorrenciaIntegracaoTest extends AbstractVotacaoIntegracaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void devePermitirApenasUmVotoEmRequisicoesConcorrentes()
            throws Exception {

        Long pautaId = criarPauta("Teste de concorrência");
        Long sessaoId = abrirSessao(pautaId, 5);

        CountDownLatch inicio = new CountDownLatch(1);

        Callable<Integer> tarefa = () -> {
            inicio.await();

            return registrarVoto(
                    pautaId,
                    "ASSOC-CONCORRENTE",
                    "SIM"
            ).getResponse().getStatus();
        };

        try (var executor = Executors.newFixedThreadPool(2)) {
            Future<Integer> primeira = executor.submit(tarefa);
            Future<Integer> segunda = executor.submit(tarefa);

            inicio.countDown();

            List<Integer> status = List.of(primeira.get(15, TimeUnit.SECONDS), segunda.get(15, TimeUnit.SECONDS));

            assertThat(status).containsExactlyInAnyOrder(201, 409);
        }

        Integer quantidade = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM voto
                        WHERE sessao_id = ?
                          AND associado_id = ?
                        """,
                Integer.class,
                sessaoId,
                "ASSOC-CONCORRENTE"
        );

        assertThat(quantidade).isEqualTo(1);
    }
}
