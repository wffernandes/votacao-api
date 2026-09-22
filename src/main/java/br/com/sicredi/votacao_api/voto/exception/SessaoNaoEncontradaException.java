package br.com.sicredi.votacao_api.voto.exception;

public class SessaoNaoEncontradaException extends RuntimeException {

    public SessaoNaoEncontradaException(Long pautaId) {
        super("Não existe sessão de votação para a pauta: " + pautaId);
    }
}
