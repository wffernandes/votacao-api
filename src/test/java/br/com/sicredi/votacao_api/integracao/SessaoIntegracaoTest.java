package br.com.sicredi.votacao_api.integracao;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SessaoIntegracaoTest extends AbstractIntegracaoTest {

    @Test
    void deveAbrirSessaoComDuracaoPadrao() throws Exception {

        Long pautaId = criarPauta("Pauta com duração padrão");

        MvcResult result = mockMvc.perform(
                        post("/api/v1/pautas/{pautaId}/sessoes", pautaId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        var json = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        OffsetDateTime inicio = OffsetDateTime.parse(
                json.get("inicio").asString()
        );

        OffsetDateTime fim = OffsetDateTime.parse(
                json.get("fim").asString()
        );

        assertThat(Duration.between(inicio, fim))
                .isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void deveAbrirSessaoComDuracaoPersonalizada() throws Exception {

        Long pautaId = criarPauta("Pauta com duração personalizada");

        MvcResult result = mockMvc.perform(
                        post("/api/v1/pautas/{pautaId}/sessoes", pautaId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "duracaoMinutos": 5
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andReturn();

        var json = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        OffsetDateTime inicio = OffsetDateTime.parse(
                json.get("inicio").asString()
        );

        OffsetDateTime fim = OffsetDateTime.parse(
                json.get("fim").asString()
        );

        assertThat(Duration.between(inicio, fim))
                .isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void naoDevePermitirDuasSessoesNaMesmaPauta() throws Exception {

        Long pautaId = criarPauta("Pauta com sessão única");

        abrirSessao(pautaId, 5);

        mockMvc.perform(
                        post("/api/v1/pautas/{pautaId}/sessoes", pautaId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "duracaoMinutos": 5
                                }
                                """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
