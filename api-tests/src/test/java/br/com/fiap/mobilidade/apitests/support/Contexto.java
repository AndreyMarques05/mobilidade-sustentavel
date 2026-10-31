package br.com.fiap.mobilidade.apitests.support;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Estado de UM cenário. O Cucumber (via PicoContainer) cria uma instância nova
 * para cada cenário e a injeta em todas as classes de passos, então nada vaza
 * de um cenário para o outro.
 *
 * Guarda o token em uso, a última resposta da API e as variáveis do cenário,
 * que os arquivos .feature referenciam como {{nome}} (ex.: "/usuarios/{{idUsuario}}").
 */
public class Contexto {

    private static final Pattern VARIAVEL = Pattern.compile("\\{\\{(\\w+)}}");

    private final Map<String, String> variaveis = new HashMap<>();
    private String token;
    private Response resposta;
    private String ultimaChamada = "(nenhuma)";
    private int contadorUsuarios = 0;

    public Contexto() {
        // Sufixo único por cenário: e-mails e nomes criados pelos testes nunca colidem,
        // mesmo rodando várias vezes contra o mesmo banco.
        variaveis.put("sufixo", UUID.randomUUID().toString().substring(0, 8));
        variaveis.put("adminEmail", Config.adminEmail());
        variaveis.put("adminSenha", Config.adminSenha());
    }

    public void guardar(String nome, Object valor) {
        variaveis.put(nome, String.valueOf(valor));
    }

    public String valor(String nome) {
        String v = variaveis.get(nome);
        if (v == null) {
            throw new IllegalStateException("Variável de cenário não definida: {{" + nome + "}}");
        }
        return v;
    }

    public Optional<String> valorOpcional(String nome) {
        return Optional.ofNullable(variaveis.get(nome));
    }

    /** Substitui cada {{variavel}} do texto pelo valor guardado no cenário. */
    public String resolver(String texto) {
        if (texto == null) {
            return null;
        }
        Matcher m = VARIAVEL.matcher(texto);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(valor(m.group(1))));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /** Sufixo único para cada usuário criado dentro do mesmo cenário. */
    public String novoSufixoDeUsuario() {
        contadorUsuarios++;
        return valor("sufixo") + "-" + contadorUsuarios;
    }

    public String token() {
        return token;
    }

    public void usarToken(String token) {
        this.token = token;
    }

    public Response resposta() {
        if (resposta == null) {
            throw new IllegalStateException("Nenhuma requisição foi feita neste cenário ainda.");
        }
        return resposta;
    }

    public Optional<Response> respostaOpcional() {
        return Optional.ofNullable(resposta);
    }

    public void registrar(String chamada, Response resposta) {
        this.ultimaChamada = chamada;
        this.resposta = resposta;
    }

    public String ultimaChamada() {
        return ultimaChamada;
    }
}
