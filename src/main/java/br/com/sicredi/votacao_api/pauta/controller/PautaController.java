package br.com.sicredi.votacao_api.pauta.controller;

import br.com.sicredi.votacao_api.pauta.dto.CriarPautaRequest;
import br.com.sicredi.votacao_api.pauta.dto.PautaResponse;
import br.com.sicredi.votacao_api.pauta.service.PautaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static br.com.sicredi.votacao_api.config.ApiPaths.API_V1;

@RestController
@RequestMapping(API_V1 + "/pautas")
@Tag(name = "Pautas",
     description = "Operações relacionadas às pautas de votação"
)
public class PautaController {

    private final PautaService pautaService;

    public PautaController(PautaService pautaService) {
        this.pautaService = pautaService;
    }

    @Operation(
            summary = "Criar uma pauta",
            description = "Cria uma nova pauta que poderá posteriormente receber uma sessão de votação."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Pauta criada com sucesso"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Dados da pauta inválidos"
    )
    @PostMapping
    public ResponseEntity<PautaResponse> criar(@Valid @RequestBody CriarPautaRequest request) {

        PautaResponse pautaResponse = pautaService.criar(request);

        URI uri = URI.create(API_V1 + "/pautas/" + pautaResponse.id());

        return ResponseEntity.created(uri).body(pautaResponse);
    }
}
