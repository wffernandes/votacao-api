package br.com.sicredi.votacao_api.voto.dto;

import br.com.sicredi.votacao_api.voto.entity.OpcaoVoto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para registro do voto de um associado")
public record RegistrarVotoRequest(

        @Schema(
                description = """
                        Identificador do associado. Para a integração
                        de elegibilidade, o identificador é tratado
                        como CPF.
                        """,
                example = "69037798098"
        )
        @NotBlank(message = "O identificador do associado é obrigatório")
        @Size(max = 100)
        String associadoId,

        @Schema(
                description = "Opção escolhida pelo associado",
                example = "SIM",
                allowableValues = {"SIM", "NAO"}
        )
        @NotNull(message = "A opção do voto é obrigatória")
        OpcaoVoto opcao
) {
}
