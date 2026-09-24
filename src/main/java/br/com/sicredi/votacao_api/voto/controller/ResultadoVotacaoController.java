package br.com.sicredi.votacao_api.voto.controller;

import br.com.sicredi.votacao_api.voto.dto.ResultadoVotacaoResponse;
import br.com.sicredi.votacao_api.voto.service.ResultadoVotacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static br.com.sicredi.votacao_api.config.ApiPaths.API_V1;

@RestController
@RequestMapping(API_V1 + "/pautas/{pautaId}/resultado")
@Tag(
        name = "Resultados",
        description = "Operações relacionadas à apuração das votações"
)
public class ResultadoVotacaoController {

    private final ResultadoVotacaoService resultadoService;

    public ResultadoVotacaoController(ResultadoVotacaoService resultadoService) {
        this.resultadoService = resultadoService;
    }

    @Operation(
            summary = "Consultar o resultado da votação",
            description = """
                Retorna a quantidade de votos SIM e NAO e o
                resultado da votação.

                O resultado somente pode ser consultado após
                o encerramento da sessão.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Resultado da votação retornado com sucesso"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Pauta ou sessão de votação não encontrada"
    )
    @ApiResponse(
            responseCode = "422",
            description = "A sessão de votação ainda está aberta"
    )
    @GetMapping
    public ResponseEntity<ResultadoVotacaoResponse> consultar(
            @PathVariable Long pautaId) {

        return ResponseEntity.ok(resultadoService.consultar(pautaId));
    }
}
