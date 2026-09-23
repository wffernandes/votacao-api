package br.com.sicredi.votacao_api.integracao;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PautaIntegracaoTest extends AbstractIntegracaoTest {

    @Test
    void deveCadastrarPautaComSucesso() throws Exception {

        String request = """
                {
                    "titulo": "Aprovação do orçamento",
                    "descricao": "Orçamento anual"
                }
                """;

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/api/v1/pautas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.titulo")
                        .value("Aprovação do orçamento"))
                .andExpect(jsonPath("$.descricao")
                        .value("Orçamento anual"));
    }

    @Test
    void deveRejeitarPautaSemTitulo() throws Exception {

        String request = """
            {
                "titulo": "",
                "descricao": "Orçamento anual"
            }
            """;

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/api/v1/pautas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Dados inválidos"))
                .andExpect(jsonPath("$.fields.titulo")
                        .exists())
                .andExpect(jsonPath("$.trace")
                        .doesNotExist());
    }
}
