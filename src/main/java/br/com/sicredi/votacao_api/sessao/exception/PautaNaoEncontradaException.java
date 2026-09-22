package br.com.sicredi.votacao_api.sessao.exception;

public class PautaNaoEncontradaException extends RuntimeException {

    public PautaNaoEncontradaException(Long pautaId) {
        super("Pauta não encontrada: " + pautaId);
    }
}
