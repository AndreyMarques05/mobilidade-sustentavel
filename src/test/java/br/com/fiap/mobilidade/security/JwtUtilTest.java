package br.com.fiap.mobilidade.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de geração e validação do token JWT usado na autenticação da API.
 */
class JwtUtilTest {

    private static final String SECRET =
            "chave-de-teste-mobilidade-sustentavel-fiap-2026-com-mais-de-256-bits";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3_600_000L);
    }

    @Test
    @DisplayName("Token gerado é válido e devolve o e-mail do usuário")
    void tokenGeradoEhValidoEGuardaOEmail() {
        String token = jwtUtil.gerarToken("andrey@fiap.com.br", "USER");

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.validarToken(token)).isTrue();
        assertThat(jwtUtil.extrairEmail(token)).isEqualTo("andrey@fiap.com.br");
    }

    @Test
    @DisplayName("Token adulterado é rejeitado")
    void tokenAdulteradoEhRejeitado() {
        String token = jwtUtil.gerarToken("andrey@fiap.com.br", "USER");
        String adulterado = token.substring(0, token.length() - 3) + "abc";

        assertThat(jwtUtil.validarToken(adulterado)).isFalse();
    }

    @Test
    @DisplayName("Texto que não é um JWT é rejeitado sem lançar exceção")
    void textoInvalidoEhRejeitado() {
        assertThat(jwtUtil.validarToken("isso-nao-e-um-token")).isFalse();
        assertThat(jwtUtil.validarToken("")).isFalse();
    }

    @Test
    @DisplayName("Token expirado é rejeitado")
    void tokenExpiradoEhRejeitado() {
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", -1_000L);
        String expirado = jwtUtil.gerarToken("andrey@fiap.com.br", "USER");

        assertThat(jwtUtil.validarToken(expirado)).isFalse();
    }

    @Test
    @DisplayName("Token assinado com outro segredo é rejeitado")
    void tokenDeOutroSegredoEhRejeitado() {
        JwtUtil outro = new JwtUtil();
        ReflectionTestUtils.setField(outro, "secret",
                "outro-segredo-completamente-diferente-para-o-teste-fiap-2026-ok");
        ReflectionTestUtils.setField(outro, "expirationMs", 3_600_000L);

        String tokenDeOutraOrigem = outro.gerarToken("intruso@fiap.com.br", "ADMIN");

        assertThat(jwtUtil.validarToken(tokenDeOutraOrigem)).isFalse();
    }
}
