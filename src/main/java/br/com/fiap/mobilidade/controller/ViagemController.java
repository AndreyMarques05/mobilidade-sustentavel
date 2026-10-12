package br.com.fiap.mobilidade.controller;

import br.com.fiap.mobilidade.dto.request.ViagemRequest;
import br.com.fiap.mobilidade.dto.response.ViagemResponse;
import br.com.fiap.mobilidade.service.ViagemService;
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
@RequestMapping("/viagens")
@RequiredArgsConstructor
@Tag(name = "Viagens", description = "Registro e gestão de viagens sustentáveis")
@SecurityRequirement(name = "bearerAuth")
public class ViagemController {

    private final ViagemService service;

    @PostMapping("/usuario/{idUsuario}")
    @Operation(summary = "Registrar nova viagem sustentável para um usuário")
    public ResponseEntity<ViagemResponse> registrar(
            @PathVariable Long idUsuario,
            @Valid @RequestBody ViagemRequest request) {
        ViagemResponse response = service.registrar(idUsuario, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/viagens/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar viagem por ID")
    public ResponseEntity<ViagemResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(summary = "Listar histórico de viagens de um usuário")
    public ResponseEntity<List<ViagemResponse>> historico(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(service.listarPorUsuario(idUsuario));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados de uma viagem")
    public ResponseEntity<ViagemResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ViagemRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir uma viagem")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
