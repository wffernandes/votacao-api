package br.com.sicredi.votacao_api.voto.exception;

public class SessaoFechadaException extends RuntimeException {

    public SessaoFechadaException(Long pautaId) {
        super("A sessão de votação da pauta não está aberta: " + pautaId );
    }
}
