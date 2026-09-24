package br.com.sicredi.votacao_api.pauta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criação de uma nova pauta")
public record CriarPautaRequest(

        @Schema(
                description = "Título da pauta",
                example = "Aprovação do orçamento anual"
        )
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 200, message = "O título deve possuir no máximo 200 caracteres")
        String titulo,

        @Schema(
                description = "Descrição opcional da pauta",
                example = "Votação para aprovação do orçamento do próximo exercício"
        )
        @Size(max = 1000, message = "A descrição deve possuir no máximo 1000 caracteres")
        String descricao
) {
}
