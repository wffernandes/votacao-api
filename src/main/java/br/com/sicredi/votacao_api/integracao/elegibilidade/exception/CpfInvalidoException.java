package br.com.sicredi.votacao_api.integracao.elegibilidade.exception;

public class CpfInvalidoException extends RuntimeException {
    public CpfInvalidoException() {
        super("CPF não encontrado no serviço de elegibilidade");
    }
}
