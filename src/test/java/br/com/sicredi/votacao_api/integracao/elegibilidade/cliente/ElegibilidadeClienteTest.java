package br.com.sicredi.votacao_api.integracao.elegibilidade.cliente;


import br.com.sicredi.votacao_api.integracao.elegibilidade.dto.SituacaoElegibilidade;
import br.com.sicredi.votacao_api.integracao.elegibilidade.exception.CpfInvalidoException;
import br.com.sicredi.votacao_api.integracao.elegibilidade.exception.ServicoElegibilidadeIndisponivelException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElegibilidadeClienteTest {

    private static final String CPF = "69037798098";

    private WireMockServer wireMockServer;
    private ElegibilidadeCliente cliente;

    @BeforeEach
    void setUp() {

        wireMockServer = new WireMockServer(options().dynamicPort());

        wireMockServer.start();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofMillis(500));

        RestClient restClient = RestClient.builder()
                .baseUrl(wireMockServer.baseUrl())
                .requestFactory(factory)
                .build();

        cliente = new ElegibilidadeCliente(restClient);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void deveRetornarAssociadoHabilitado() {

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

        var response = cliente.consultar(CPF);

        assertThat(response.status()).isEqualTo(SituacaoElegibilidade.ABLE_TO_VOTE);

        wireMockServer.verify(1,getRequestedFor(urlEqualTo("/users/" + CPF)));
    }

    @Test
    void deveRetornarAssociadoNaoHabilitado() {

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

        var response = cliente.consultar(CPF);

        assertThat(response.status()).isEqualTo(SituacaoElegibilidade.UNABLE_TO_VOTE);
    }

    @Test
    void deveTratarCpfNaoEncontrado() {

        wireMockServer.stubFor(
                get(urlEqualTo("/users/" + CPF))
                        .willReturn(
                                aResponse().withStatus(404)
                        )
        );

        assertThatThrownBy(() -> cliente.consultar(CPF))
                .isInstanceOf(CpfInvalidoException.class);
    }

    @Test
    void deveTratarServicoIndisponivel() {

        wireMockServer.stubFor(
                get(urlEqualTo("/users/" + CPF))
                        .willReturn(
                                aResponse().withStatus(503)
                        )
        );

        assertThatThrownBy(() -> cliente.consultar(CPF))
                .isInstanceOf(ServicoElegibilidadeIndisponivelException.class);
    }

    @Test
    void deveTratarTimeout() {

        wireMockServer.stubFor(
                get(urlEqualTo("/users/" + CPF))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withFixedDelay(1500)
                                        .withBody("""
                                                {
                                                  "status": "ABLE_TO_VOTE"
                                                }
                                                """)
                        )
        );

        assertThatThrownBy(() -> cliente.consultar(CPF))
                .isInstanceOf(ServicoElegibilidadeIndisponivelException.class);
    }

    @Test
    void deveTratarRespostaSemStatus() {

        wireMockServer.stubFor(
                get(urlEqualTo("/users/" + CPF))
                        .willReturn(
                                okJson("{}")
                        )
        );

        assertThatThrownBy(() -> cliente.consultar(CPF))
                .isInstanceOf(ServicoElegibilidadeIndisponivelException.class);
    }

    @Test
    void deveTratarJsonInvalido() {

        wireMockServer.stubFor(
                get(urlEqualTo("/users/" + CPF))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody("""
                                            {
                                              "status":
                                            }
                                            """)
                        )
        );

        assertThatThrownBy(() -> cliente.consultar(CPF))
                .isInstanceOf(ServicoElegibilidadeIndisponivelException.class);
    }

    @Test
    void deveTratarRespostaSemCorpo() {

        wireMockServer.stubFor(
                get(urlEqualTo("/users/" + CPF))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                        )
        );

        assertThatThrownBy(() -> cliente.consultar(CPF)).isInstanceOf(ServicoElegibilidadeIndisponivelException.class);

        wireMockServer.verify(1,getRequestedFor(urlEqualTo("/users/" + CPF))        );
    }
}
