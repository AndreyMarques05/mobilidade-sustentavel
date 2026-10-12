package br.com.fiap.mobilidade.controller;

import br.com.fiap.mobilidade.dto.request.RecompensaRequest;
import br.com.fiap.mobilidade.dto.response.RecompensaResponse;
import br.com.fiap.mobilidade.service.RecompensaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/recompensas")
@RequiredArgsConstructor
@Tag(name = "Recompensas", description = "Sistema de recompensas para transporte sustentável")
@SecurityRequirement(name = "bearerAuth")
public class RecompensaController {

    private final RecompensaService service;

    @GetMapping
    @Operation(summary = "Listar recompensas disponíveis")
    public ResponseEntity<List<RecompensaResponse>> listar() {
        return ResponseEntity.ok(service.listarAtivas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar recompensa por ID")
    public ResponseEntity<RecompensaResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar nova recompensa (ADMIN)")
    public ResponseEntity<RecompensaResponse> criar(@Valid @RequestBody RecompensaRequest request) {
        RecompensaResponse response = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar recompensa (ADMIN)")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/usuario/{idUsuario}/resgatar/{idRecompensa}")
    @Operation(summary = "Resgatar recompensa com pontos acumulados")
    public ResponseEntity<String> resgatar(
            @PathVariable Long idUsuario,
            @PathVariable Long idRecompensa) {
        return ResponseEntity.ok(service.resgatar(idUsuario, idRecompensa));
    }
}
