package br.com.sicredi.votacao_api.sessao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "Configuração para abertura de uma sessão de votação")
public record AbrirSessaoRequest(

        @Schema(
                description = """
                        Duração da sessão em minutos.
                        Quando não informada, a duração padrão é de 1 minuto.
                        """,
                example = "5",
                minimum = "1"
        )
        @Positive(message = "A duração deve ser maior que zero")
        Integer duracaoMinutos
) {
}
