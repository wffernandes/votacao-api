package br.com.sicredi.votacao_api.integracao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AbstractIntegracaoTest.TestcontainersConfiguration.class)
public abstract class AbstractIntegracaoTest {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @TestConfiguration(proxyBeanMethods = false)
    static class TestcontainersConfiguration {

        @Bean(destroyMethod = "close")
        @ServiceConnection
        @SuppressWarnings("resource")
        PostgreSQLContainer postgresContainer() {

            return new PostgreSQLContainer("postgres:17-alpine")
                    .withDatabaseName("votacao_test")
                    .withUsername("test")
                    .withPassword("test");
        }
    }

    protected Long criarPauta(String titulo) throws Exception {

        String request = objectMapper.writeValueAsString(
                Map.of(
                        "titulo", titulo,
                        "descricao", "Pauta criada pelo teste"
                )
        );

        MvcResult result = mockMvc.perform(
                        post("/api/v1/pautas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        return response.get("id").asLong();
    }

    protected Long abrirSessao(Long pautaId, Integer duracaoMinutos) throws Exception {

        String request = objectMapper.writeValueAsString(
                Map.of("duracaoMinutos", duracaoMinutos));

        MvcResult result = mockMvc.perform(
                        post("/api/v1/pautas/{pautaId}/sessoes", pautaId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        return response.get("id").asLong();
    }

    protected MvcResult registrarVoto(Long pautaId, String associadoId, String opcao ) throws Exception {

        String request = objectMapper.writeValueAsString(
                Map.of(
                        "associadoId", associadoId,
                        "opcao", opcao
                )
        );

        return mockMvc.perform(
                        post("/api/v1/pautas/{pautaId}/votos", pautaId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andReturn();
    }
}