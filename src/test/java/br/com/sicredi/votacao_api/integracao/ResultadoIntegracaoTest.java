package br.com.sicredi.votacao_api.integracao;

import br.com.sicredi.votacao_api.integracao.util.ControlledClockConfig;
import br.com.sicredi.votacao_api.integracao.util.MutableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(ControlledClockConfig.class)
class ResultadoIntegracaoTest extends AbstractVotacaoIntegracaoTest {

    private static final Instant INSTANTE_INICIAL =
            Instant.parse("2026-09-23T10:00:00Z");

    @Autowired
    private MutableClock clock;

    @BeforeEach
    void prepararRelogio() {
        clock.definirInstante(INSTANTE_INICIAL);
    }

    @Test
    void naoDeveDisponibilizarResultadoComSessaoAberta()
            throws Exception {

        Long pautaId = criarPauta("Resultado com sessão aberta");

        abrirSessao(pautaId, 5);

        mockMvc.perform(
                        get("/api/v1/pautas/{pautaId}/resultado", pautaId)
                )
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message")
                        .value(
                                "O resultado da pauta %d estará disponível após o encerramento da sessão"
                                        .formatted(pautaId)
                        ));
    }

    @Test
    void deveRetornarResultadoAprovado() throws Exception {

        Long pautaId = criarPauta("Resultado aprovado");

        abrirSessao(pautaId, 5);

        registrarVoto(pautaId, "ASSOC-001", "SIM");
        registrarVoto(pautaId, "ASSOC-002", "SIM");
        registrarVoto(pautaId, "ASSOC-003", "SIM");
        registrarVoto(pautaId, "ASSOC-004", "NAO");

        clock.avancar(Duration.ofMinutes(5));

        mockMvc.perform(
                        get("/api/v1/pautas/{pautaId}/resultado", pautaId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pautaId").value(pautaId))
                .andExpect(jsonPath("$.totalVotos").value(4))
                .andExpect(jsonPath("$.totalSim").value(3))
                .andExpect(jsonPath("$.totalNao").value(1))
                .andExpect(jsonPath("$.resultado")
                        .value("APROVADA"));
    }

    @Test
    void deveRetornarResultadoRejeitado() throws Exception {

        Long pautaId = criarPauta("Resultado rejeitado");

        abrirSessao(pautaId, 5);

        registrarVoto(pautaId, "ASSOC-101", "SIM");
        registrarVoto(pautaId, "ASSOC-102", "NAO");
        registrarVoto(pautaId, "ASSOC-103", "NAO");
        registrarVoto(pautaId, "ASSOC-104", "NAO");

        clock.avancar(Duration.ofMinutes(5));

        mockMvc.perform(
                        get("/api/v1/pautas/{pautaId}/resultado", pautaId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotos").value(4))
                .andExpect(jsonPath("$.totalSim").value(1))
                .andExpect(jsonPath("$.totalNao").value(3))
                .andExpect(jsonPath("$.resultado")
                        .value("REJEITADA"));
    }

    @Test
    void deveRetornarEmpate() throws Exception {

        Long pautaId = criarPauta("Resultado empatado");

        abrirSessao(pautaId, 5);

        registrarVoto(pautaId, "ASSOC-201", "SIM");
        registrarVoto(pautaId, "ASSOC-202", "SIM");
        registrarVoto(pautaId, "ASSOC-203", "NAO");
        registrarVoto(pautaId, "ASSOC-204", "NAO");

        clock.avancar(Duration.ofMinutes(5));

        mockMvc.perform(
                        get("/api/v1/pautas/{pautaId}/resultado", pautaId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotos").value(4))
                .andExpect(jsonPath("$.totalSim").value(2))
                .andExpect(jsonPath("$.totalNao").value(2))
                .andExpect(jsonPath("$.resultado")
                        .value("EMPATE"));
    }

    @Test
    void deveRetornarResultadoSemVotos() throws Exception {

        Long pautaId = criarPauta("Resultado sem votos");

        abrirSessao(pautaId, 5);

        clock.avancar(Duration.ofMinutes(5));

        mockMvc.perform(
                        get("/api/v1/pautas/{pautaId}/resultado", pautaId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotos").value(0))
                .andExpect(jsonPath("$.totalSim").value(0))
                .andExpect(jsonPath("$.totalNao").value(0))
                .andExpect(jsonPath("$.resultado")
                        .value("EMPATE"));
    }
}
