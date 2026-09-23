package br.com.sicredi.votacao_api.integracao.elegibilidade.cliente;

import br.com.sicredi.votacao_api.integracao.elegibilidade.dto.ElegibilidadeResponse;
import br.com.sicredi.votacao_api.integracao.elegibilidade.exception.CpfInvalidoException;
import br.com.sicredi.votacao_api.integracao.elegibilidade.exception.ServicoElegibilidadeIndisponivelException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ElegibilidadeCliente {

    private final RestClient restClient;

    public ElegibilidadeCliente(@Qualifier("elegibilidadeRestClient") RestClient restClient) {

        this.restClient = restClient;
    }

    public ElegibilidadeResponse consultar(String cpf) {
        try {
            ElegibilidadeResponse response = restClient
                    .get()
                    .uri("/users/{cpf}", cpf)
                    .retrieve()
                    .onStatus(status -> status.value() == 404,
                            (request, clientResponse) -> {
                                throw new CpfInvalidoException();
                            }
                    )
                    .body(ElegibilidadeResponse.class);

            if (response == null || response.status() == null) {
                throw new ServicoElegibilidadeIndisponivelException();
            }

            return response;

        } catch (CpfInvalidoException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new ServicoElegibilidadeIndisponivelException(exception);
        }
    }
}
