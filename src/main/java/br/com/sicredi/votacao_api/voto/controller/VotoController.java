package br.com.sicredi.votacao_api.voto.controller;

import br.com.sicredi.votacao_api.voto.dto.RegistrarVotoRequest;
import br.com.sicredi.votacao_api.voto.dto.VotoResponse;
import br.com.sicredi.votacao_api.voto.service.VotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static br.com.sicredi.votacao_api.config.ApiPaths.API_V1;

@RestController
@RequestMapping(API_V1 + "/pautas/{pautaId}/votos")
@Tag(
        name = "Votos",
        description = "Operações relacionadas ao registro de votos"
)
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    @Operation(
            summary = "Registrar o voto de um associado",
            description = """
                    Registra um voto SIM ou NAO em uma sessão aberta.
                    
                    Cada associado pode votar apenas uma vez por pauta.
                    Para a integração de elegibilidade, o identificador
                    do associado é tratado como CPF e consultado no
                    serviço externo antes do registro do voto.
                    """
    )
    @ApiResponse(
            responseCode = "201",
            description = "Voto registrado com sucesso"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Dados do voto inválidos"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Pauta, sessão ou CPF não encontrado"
    )
    @ApiResponse(
            responseCode = "409",
            description = "O associado já votou nesta sessão"
    )
    @ApiResponse(
            responseCode = "422",
            description = "Sessão encerrada ou associado não habilitado para votar"
    )
    @ApiResponse(
            responseCode = "503",
            description = "Serviço externo de elegibilidade indisponível"
    )
    @PostMapping
    public ResponseEntity<VotoResponse> registrar(@Parameter(description = "Identificador da pauta", example = "1")
                                                  @PathVariable Long pautaId,
                                                  @Valid @RequestBody RegistrarVotoRequest request) {

        VotoResponse voto = votoService.registrar(pautaId, request);

        URI uri = URI.create(API_V1 + "/pautas/%d/votos/%d".formatted(pautaId, voto.id()));

        return ResponseEntity.created(uri).body(voto);
    }
}
