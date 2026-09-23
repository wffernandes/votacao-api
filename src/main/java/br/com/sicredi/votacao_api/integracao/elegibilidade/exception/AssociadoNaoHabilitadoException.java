package br.com.sicredi.votacao_api.integracao.elegibilidade.exception;

public class AssociadoNaoHabilitadoException extends RuntimeException {
    public AssociadoNaoHabilitadoException() {
        super("O associado não está habilitado para votar");
    }
}
