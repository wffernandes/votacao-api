package br.com.sicredi.votacao_api.sessao.dto;

import jakarta.validation.constraints.Positive;

public record AbrirSessaoRequest(

        @Positive(message = "A duração deve ser maior que zero")
        Integer duracaoMinutos
) {
}
