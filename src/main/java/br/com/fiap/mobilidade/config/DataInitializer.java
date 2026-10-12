package br.com.fiap.mobilidade.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        String hash = passwordEncoder.encode("admin123");

        int rows = jdbcTemplate.update(
                "UPDATE tb_usuario SET senha = ? WHERE email = ?",
                hash, "admin@mobilidade.com"
        );

        if (rows > 0) {
            log.info("==> Senha do admin atualizada com sucesso.");
        } else {
            log.warn("==> Admin nao encontrado na base para atualizar senha.");
        }
    }
}
