package br.com.sicredi.votacao_api.integracao.elegibilidade.service;

import br.com.sicredi.votacao_api.integracao.elegibilidade.cliente.ElegibilidadeCliente;
import br.com.sicredi.votacao_api.integracao.elegibilidade.dto.ElegibilidadeResponse;
import br.com.sicredi.votacao_api.integracao.elegibilidade.dto.SituacaoElegibilidade;
import br.com.sicredi.votacao_api.integracao.elegibilidade.exception.AssociadoNaoHabilitadoException;
import org.springframework.stereotype.Service;

@Service
public class ElegibilidadeAssociadoService {

    private final ElegibilidadeCliente elegibilidadeCliente;

    public ElegibilidadeAssociadoService(ElegibilidadeCliente elegibilidadeCliente) {
        this.elegibilidadeCliente = elegibilidadeCliente;
    }

    public void validar(String cpf) {

        ElegibilidadeResponse response = elegibilidadeCliente.consultar(cpf);

        if (response.status() != SituacaoElegibilidade.ABLE_TO_VOTE) {

            throw new AssociadoNaoHabilitadoException();
        }
    }
}
