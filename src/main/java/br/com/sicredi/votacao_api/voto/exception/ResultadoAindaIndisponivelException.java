package br.com.sicredi.votacao_api.voto.exception;

public class ResultadoAindaIndisponivelException extends RuntimeException {
    public ResultadoAindaIndisponivelException(Long pautaId) {
        super("O resultado da pauta %d estará disponível após o encerramento da sessão".formatted(pautaId));
    }
}