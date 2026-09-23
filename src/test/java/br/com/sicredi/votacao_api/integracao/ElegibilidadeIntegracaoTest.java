package br.com.sicredi.votacao_api.integracao;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

class ElegibilidadeIntegracaoTest extends AbstractIntegracaoTest {

    private static final String CPF = "69037798098";

    private static final WireMockServer wireMockServer;

    static {
        wireMockServer =
                new WireMockServer(options().dynamicPort());

        wireMockServer.start();
    }

    @DynamicPropertySource
    static void configurarElegibilidade(DynamicPropertyRegistry registry) {
        registry.add("integracao.elegibilidade.base-url", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeEach
    void resetarWireMock() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void pararWireMock() {
        wireMockServer.stop();
    }

    @Test
    void devePermitirVotoQuandoAssociadoEstaHabilitado()
            throws Exception {

        wireMockServer.stubFor(
                get(urlEqualTo("/users/" + CPF))
                        .willReturn(
                                okJson("""
                                        {
                                          "status": "ABLE_TO_VOTE"
                                        }
                                        """)
                        )
        );

        Long pautaId = criarPauta("Teste de elegibilidade integração");

        abrirSessao(pautaId, 5);

        MvcResult resultado = registrarVoto(pautaId, CPF, "SIM");

        assertThat(resultado.getResponse().getStatus()).isEqualTo(HttpStatus.CREATED.value());

        wireMockServer.verify(
                1,
                getRequestedFor(
                        urlEqualTo("/users/" + CPF)
                )
        );
    }

    @Test
    void deveRejeitarVotoQuandoAssociadoNaoEstaHabilitado()
            throws Exception {

        wireMockServer.stubFor(
                get(urlEqualTo("/users/" + CPF))
                        .willReturn(
                                okJson("""
                                        {
                                          "status": "UNABLE_TO_VOTE"
                                        }
                                        """)
                        )
        );

        Long pautaId = criarPauta("Teste de elegibilidade integração");

        abrirSessao(pautaId, 5);

        MvcResult resultado = registrarVoto(pautaId, CPF, "SIM");

        assertThat(resultado.getResponse().getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
    }

    @Test
    void deveRetornarNotFoundQuandoCpfNaoExiste()
            throws Exception {

        wireMockServer.stubFor(
                get(urlEqualTo("/users/" + CPF))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.NOT_FOUND.value())
                        )
        );

        Long pautaId = criarPauta("Teste de elegibilidade integração");

        abrirSessao(pautaId, 5);

        MvcResult resultado = registrarVoto(pautaId, CPF, "SIM");

        assertThat(resultado.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deveRetornarServiceUnavailableQuandoServicoExternoFalhar()
            throws Exception {

        wireMockServer.stubFor(
                get(urlEqualTo("/users/" + CPF))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.SERVICE_UNAVAILABLE.value())
                        )
        );

        Long pautaId = criarPauta("Teste de elegibilidade integração");

        abrirSessao(pautaId, 5);

        MvcResult resultado = registrarVoto(pautaId, CPF, "SIM");

        assertThat(resultado.getResponse().getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
    }
}
