package br.com.sicredi.votacao_api.voto.controller;

import br.com.sicredi.votacao_api.voto.dto.RegistrarVotoRequest;
import br.com.sicredi.votacao_api.voto.dto.VotoResponse;
import br.com.sicredi.votacao_api.voto.service.VotoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/votos")
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    @PostMapping
    public ResponseEntity<VotoResponse> registrar(@PathVariable Long pautaId,
                                                  @Valid @RequestBody RegistrarVotoRequest request) {

        VotoResponse voto = votoService.registrar(pautaId, request);

        URI uri = URI.create("/api/v1/pautas/%d/votos/%d".formatted(pautaId, voto.id()));

        return ResponseEntity.created(uri).body(voto);
    }
}
