package br.com.sicredi.votacao_api.integracao.elegibilidade.exception;

public class ServicoElegibilidadeIndisponivelException extends RuntimeException {

    public ServicoElegibilidadeIndisponivelException(Throwable causa) {
        super("O serviço de elegibilidade está indisponível", causa);
    }

    public ServicoElegibilidadeIndisponivelException() {
        super("O serviço de elegibilidade retornou uma resposta inválida");
    }
}
