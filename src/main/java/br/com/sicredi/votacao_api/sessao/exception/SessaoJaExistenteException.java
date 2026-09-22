package br.com.sicredi.votacao_api.sessao.exception;

public class SessaoJaExistenteException extends RuntimeException {

    public SessaoJaExistenteException(Long pautaId) {
        super("Já existe uma sessão de votação para a pauta: " + pautaId);
    }
}
