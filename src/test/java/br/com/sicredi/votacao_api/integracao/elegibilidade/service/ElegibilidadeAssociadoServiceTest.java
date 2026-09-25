package br.com.sicredi.votacao_api.integracao.elegibilidade.service;

import br.com.sicredi.votacao_api.integracao.elegibilidade.cliente.ElegibilidadeCliente;
import br.com.sicredi.votacao_api.integracao.elegibilidade.dto.ElegibilidadeResponse;
import br.com.sicredi.votacao_api.integracao.elegibilidade.dto.SituacaoElegibilidade;
import br.com.sicredi.votacao_api.integracao.elegibilidade.exception.AssociadoNaoHabilitadoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElegibilidadeAssociadoServiceTest {

    private static final String CPF = "69037798098";

    @Mock
    private ElegibilidadeCliente elegibilidadeClient;

    @InjectMocks
    private ElegibilidadeAssociadoService service;

    @Test
    void devePermitirAssociadoHabilitado() {

        when(elegibilidadeClient.consultar(CPF)).thenReturn(new ElegibilidadeResponse(SituacaoElegibilidade.ABLE_TO_VOTE));

        assertThatCode(() -> service.validar(CPF)).doesNotThrowAnyException();

        verify(elegibilidadeClient).consultar(CPF);
    }

    @Test
    void deveRejeitarAssociadoNaoHabilitado() {

        when(elegibilidadeClient.consultar(CPF)).thenReturn(new ElegibilidadeResponse(SituacaoElegibilidade.UNABLE_TO_VOTE));

        assertThatThrownBy(() -> service.validar(CPF))
                .isInstanceOf(AssociadoNaoHabilitadoException.class)
                .hasMessage("O associado não está habilitado para votar");

        verify(elegibilidadeClient).consultar(CPF);
    }
}
