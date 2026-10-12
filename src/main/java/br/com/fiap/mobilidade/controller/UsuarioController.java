package br.com.fiap.mobilidade.controller;

import br.com.fiap.mobilidade.dto.request.UsuarioRequest;
import br.com.fiap.mobilidade.dto.response.ImpactoResponse;
import br.com.fiap.mobilidade.dto.response.RankingResponse;
import br.com.fiap.mobilidade.dto.response.UsuarioResponse;
import br.com.fiap.mobilidade.service.RecompensaService;
import br.com.fiap.mobilidade.service.UsuarioService;
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
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Cadastro e gestão de usuários")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService service;
    private final RecompensaService recompensaService;

    @PostMapping
    @Operation(summary = "Registrar novo usuário (público)")
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = service.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os usuários (somente ADMIN)")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UsuarioResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar usuário")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        service.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/impacto")
    @Operation(summary = "Consultar impacto ambiental de um usuário")
    public ResponseEntity<ImpactoResponse> impacto(@PathVariable Long id) {
        return ResponseEntity.ok(service.calcularImpacto(id));
    }

    @GetMapping("/ranking")
    @Operation(summary = "Ranking de sustentabilidade por pontos")
    public ResponseEntity<List<RankingResponse>> ranking() {
        return ResponseEntity.ok(service.ranking());
    }

    @PostMapping("/{id}/recompensas/{recompensaId}/resgatar")
    @Operation(summary = "Resgatar uma recompensa com pontos acumulados")
    public ResponseEntity<String> resgatar(
            @PathVariable Long id,
            @PathVariable Long recompensaId) {
        return ResponseEntity.ok(recompensaService.resgatar(id, recompensaId));
    }
}
