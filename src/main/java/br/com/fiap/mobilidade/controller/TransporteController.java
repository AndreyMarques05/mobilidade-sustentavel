package br.com.fiap.mobilidade.controller;

import br.com.fiap.mobilidade.dto.request.TransporteRequest;
import br.com.fiap.mobilidade.dto.response.TransporteResponse;
import br.com.fiap.mobilidade.service.TransporteService;
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
@RequestMapping("/transportes")
@RequiredArgsConstructor
@Tag(name = "Tipos de Transporte", description = "Cadastro e consulta de modais de transporte sustentável")
@SecurityRequirement(name = "bearerAuth")
public class TransporteController {

    private final TransporteService service;

    @GetMapping
    @Operation(summary = "Listar todos os tipos de transporte ativos")
    public ResponseEntity<List<TransporteResponse>> listar() {
        return ResponseEntity.ok(service.listarAtivos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tipo de transporte por ID")
    public ResponseEntity<TransporteResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar novo tipo de transporte (ADMIN)")
    public ResponseEntity<TransporteResponse> criar(@Valid @RequestBody TransporteRequest request) {
        TransporteResponse response = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tipo de transporte (ADMIN)")
    public ResponseEntity<TransporteResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TransporteRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar tipo de transporte (ADMIN)")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
