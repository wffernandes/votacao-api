package br.com.sicredi.votacao_api.voto.dto;

import br.com.sicredi.votacao_api.voto.entity.OpcaoVoto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarVotoRequest(

    @NotBlank(message = "O identificador do associado é obrigatório")
    @Size(max = 100)
    String associadoId,

    @NotNull(message = "A opção do voto é obrigatória")
    OpcaoVoto opcao
) {
}
