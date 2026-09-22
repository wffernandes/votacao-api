package br.com.sicredi.votacao_api.pauta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarPautaRequest(

        @NotBlank(message = "O título é obrigatório")
        @Size(max = 200, message = "O título deve possuir no máximo 200 caracteres")
        String titulo,

        @Size(max = 1000, message = "A descrição deve possuir no máximo 1000 caracteres")
        String descricao
) {
}
