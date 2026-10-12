package br.com.fiap.mobilidade.service;

import br.com.fiap.mobilidade.dto.request.LoginRequest;
import br.com.fiap.mobilidade.dto.response.LoginResponse;
import br.com.fiap.mobilidade.exception.BusinessException;
import br.com.fiap.mobilidade.model.Usuario;
import br.com.fiap.mobilidade.repository.UsuarioRepository;
import br.com.fiap.mobilidade.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Credenciais inválidas."));

        if (Boolean.FALSE.equals(usuario.getAtivo())) {
            throw new BusinessException("Credenciais inválidas.");
        }

        if (!passwordEncoder.matches(request.senha(), usuario.getSenha())) {
            throw new BusinessException("Credenciais inválidas.");
        }

        String token = jwtUtil.gerarToken(usuario.getEmail(), usuario.getRole().name());
        return new LoginResponse(token, "Bearer", usuario.getId(), usuario.getNome(),
                usuario.getEmail(), usuario.getRole().name());
    }
}
