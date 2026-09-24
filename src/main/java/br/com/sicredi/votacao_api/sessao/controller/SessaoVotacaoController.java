package br.com.sicredi.votacao_api.sessao.controller;

import br.com.sicredi.votacao_api.sessao.dto.AbrirSessaoRequest;
import br.com.sicredi.votacao_api.sessao.dto.SessaoVotacaoResponse;
import br.com.sicredi.votacao_api.sessao.service.SessaoVotacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static br.com.sicredi.votacao_api.config.ApiPaths.API_V1;

@RestController
@RequestMapping(API_V1 + "/pautas/{pautaId}/sessoes")
@Tag(
        name = "Sessões de votação",
        description = "Operações relacionadas à abertura das sessões de votação"
)
public class SessaoVotacaoController {

    private final SessaoVotacaoService sessaoService;

    public SessaoVotacaoController(SessaoVotacaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @Operation(
            summary = "Abrir uma sessão de votação",
            description = """
                    Abre a sessão de votação de uma pauta.
                    Quando a duração não é informada, a sessão utiliza
                    a duração padrão de 1 minuto.
                    """
    )
    @ApiResponse(
            responseCode = "201",
            description = "Sessão de votação aberta com sucesso"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Dados da sessão inválidos"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Pauta não encontrada"
    )
    @ApiResponse(
            responseCode = "409",
            description = "A pauta já possui uma sessão de votação"
    )
    @PostMapping
    public ResponseEntity<SessaoVotacaoResponse> abrir(
            @PathVariable Long pautaId,
            @Valid @RequestBody AbrirSessaoRequest request) {

        SessaoVotacaoResponse sessao = sessaoService.abrir(pautaId, request);

        URI uri = URI.create(API_V1 + "/pautas/%d/sessoes/%d".formatted(pautaId, sessao.id()));

        return ResponseEntity.created(uri).body(sessao);
    }
}
