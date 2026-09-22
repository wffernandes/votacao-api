package br.com.sicredi.votacao_api.voto.exception;

public class AssociadoJaVotouException extends RuntimeException {

    public AssociadoJaVotouException(Long pautaId, String associadoId) {
        super("O associado %s já votou na pauta %d ".formatted(associadoId, pautaId));
    }
}
