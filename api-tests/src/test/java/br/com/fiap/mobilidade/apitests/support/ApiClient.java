package br.com.fiap.mobilidade.apitests.support;

import io.restassured.RestAssured;
import io.restassured.config.DecoderConfig;
import io.restassured.config.EncoderConfig;
import io.restassured.config.JsonConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.path.json.config.JsonPathConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP dos testes, sobre o REST Assured.
 *
 * Há dois tipos de chamada:
 *  - executar(): a ação do cenário (o "Quando"). A resposta vira "a resposta"
 *    que os passos "Então" verificam.
 *  - preparar(): montagem de dados (o "Dado"). Não substitui "a resposta",
 *    para a preparação não interferir no que está sendo verificado.
 */
public class ApiClient {

    static {
        RestAssured.config = RestAssuredConfig.config()
                // Decimais como double (o padrão do REST Assured é float, que perde precisão)
                .jsonConfig(JsonConfig.jsonConfig()
                        .numberReturnType(JsonPathConfig.NumberReturnType.DOUBLE))
                .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset("UTF-8"))
                .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset("UTF-8"));
    }

    /** Token do administrador, obtido uma vez por execução. */
    private static String tokenAdmin;

    public static final String SENHA_PADRAO = "Senha@123";

    private final Contexto ctx;

    public ApiClient(Contexto ctx) {
        this.ctx = ctx;
    }

    public Response executar(String metodo, String caminho, String corpo) {
        Response r = enviar(ctx.token(), metodo, caminho, corpo);
        ctx.registrar(metodo + " " + caminho + (corpo == null ? "" : "\n" + corpo), r);
        return r;
    }

    public Response preparar(String token, String metodo, String caminho, String corpo) {
        return enviar(token, metodo, caminho, corpo);
    }

    public synchronized String tokenAdmin() {
        if (tokenAdmin == null) {
            tokenAdmin = login(Config.adminEmail(), Config.adminSenha());
        }
        return tokenAdmin;
    }

    public String login(String email, String senha) {
        Response r = enviar(null, "POST", "/auth/login",
                "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}");
        exigir(r, 200, "login de " + email);
        return r.jsonPath().getString("token");
    }

    /**
     * Cadastra um usuário comum com e-mail único e faz login.
     * Devolve {id, email, senha, token}.
     */
    public Map<String, String> novoUsuarioAutenticado() {
        String sufixo = ctx.novoSufixoDeUsuario();
        String email = "bdd." + sufixo + "@teste.com";
        String corpo = "{\"nome\":\"Usuario BDD " + sufixo + "\",\"email\":\"" + email
                + "\",\"senha\":\"" + SENHA_PADRAO + "\",\"tipo\":\"PESSOA_FISICA\"}";
        Response r = enviar(null, "POST", "/usuarios", corpo);
        exigir(r, 201, "cadastro do usuário de teste");
        String id = String.valueOf(r.jsonPath().getLong("id"));
        String token = login(email, SENHA_PADRAO);
        return Map.of("id", id, "email", email, "senha", SENHA_PADRAO, "token", token);
    }

    /** Busca o id de um item pelo nome numa coleção (/transportes, /recompensas). */
    public long idPorNome(String colecao, String nome) {
        Response r = enviar(tokenAdmin(), "GET", colecao, null);
        exigir(r, 200, "listagem de " + colecao);
        List<Map<String, Object>> itens = r.jsonPath().getList("$");
        return itens.stream()
                .filter(i -> nome.equals(i.get("nome")))
                .map(i -> ((Number) i.get("id")).longValue())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Nenhum item ativo com nome '" + nome + "' em " + colecao));
    }

    public static void exigir(Response r, int status, String oQue) {
        if (r.statusCode() != status) {
            throw new IllegalStateException("Preparação falhou (" + oQue + "): esperado HTTP "
                    + status + ", veio " + r.statusCode() + " -> " + r.asString());
        }
    }

    private Response enviar(String token, String metodo, String caminho, String corpo) {
        RequestSpecification req = RestAssured.given().baseUri(Config.baseUrl());
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        if (corpo != null) {
            req.contentType(ContentType.JSON).body(corpo);
        }
        return req.request(metodo, caminho);
    }
}
