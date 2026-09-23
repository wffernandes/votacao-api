package br.com.sicredi.votacao_api.integracao;

import br.com.sicredi.votacao_api.integracao.util.ControlledClockConfig;
import br.com.sicredi.votacao_api.integracao.util.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Import(ControlledClockConfig.class)
class VotoTemporalIntegracaoTest extends AbstractIntegracaoTest {

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void prepararRelogio() {
        clock.definirInstante(Instant.parse("2026-09-23T10:00:00Z"));
    }

    @Test
    void naoDevePermitirVotoAposEncerramento() throws Exception {

        Long pautaId = criarPauta("Voto após encerramento" );

        abrirSessao(pautaId, 5);

        clock.avancar(Duration.ofMinutes(5));

        var response = registrarVoto(pautaId, "ASSOC-301", "SIM");

        assertThat(response.getResponse().getStatus())
                .isEqualTo(422);
    }
}
