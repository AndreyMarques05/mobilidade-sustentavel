package br.com.fiap.mobilidade.apitests.steps;

import br.com.fiap.mobilidade.apitests.support.ApiClient;
import br.com.fiap.mobilidade.apitests.support.Contexto;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Passos declarativos, na linguagem do produto. Usados nos arquivos de
 * features/negocio: descrevem o comportamento esperado ("registro uma viagem",
 * "tento resgatar a recompensa") sem falar de verbos HTTP nem de JSON.
 * Os detalhes técnicos ficam aqui dentro.
 */
public class NegocioSteps {

    private static final double TOLERANCIA = 0.0001;

    private final Contexto ctx;
    private final ApiClient api;

    public NegocioSteps(Contexto ctx, ApiClient api) {
        this.ctx = ctx;
        this.api = api;
    }

    // ------------------------------------------------------------ viagens

    @Quando("registro uma viagem de {decimal} km usando {string}")
    public void registroViagem(Double km, String modal) {
        long idModal = api.idPorNome("/transportes", modal);
        api.executar("POST", "/viagens/usuario/" + ctx.valor("idUsuario"), corpoViagem(idModal, km));
    }

    @Dado("que registrei uma viagem de {decimal} km usando {string}")
    public void registreiViagem(Double km, String modal) {
        long idModal = api.idPorNome("/transportes", modal);
        Response r = api.preparar(ctx.token(), "POST",
                "/viagens/usuario/" + ctx.valor("idUsuario"), corpoViagem(idModal, km));
        ApiClient.exigir(r, 201, "registro de viagem de " + km + " km em " + modal);
        ctx.guardar("idViagem", r.jsonPath().getLong("id"));
    }

    @Dado("que outro usuário registrou uma viagem de {decimal} km usando {string}")
    public void outroUsuarioRegistrou(Double km, String modal) {
        Map<String, String> outro = api.novoUsuarioAutenticado();
        long idModal = api.idPorNome("/transportes", modal);
        Response r = api.preparar(outro.get("token"), "POST",
                "/viagens/usuario/" + outro.get("id"), corpoViagem(idModal, km));
        ApiClient.exigir(r, 201, "viagem do outro usuário");
        ctx.guardar("idViagemDoOutro", r.jsonPath().getLong("id"));
    }

    @Quando("corrijo a viagem para {decimal} km usando {string}")
    public void corrijoViagem(Double km, String modal) {
        long idModal = api.idPorNome("/transportes", modal);
        api.executar("PUT", "/viagens/" + ctx.valor("idViagem"), corpoViagem(idModal, km));
    }

    @Entao("a viagem corrigida deve registrar {decimal} kg de CO2 economizado")
    public void co2DaViagemCorrigida(Double kg) {
        Response r = ctx.resposta();
        assertEquals(200, r.statusCode(), () -> "Viagem não corrigida: " + r.asString());
        r.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/viagem-response.json"));
        assertEquals(kg, r.jsonPath().getDouble("co2EconomizadoKg"), TOLERANCIA, "CO2 economizado");
    }

    @Quando("tento excluir a viagem do outro usuário")
    public void excluirViagemDoOutro() {
        api.executar("DELETE", "/viagens/" + ctx.valor("idViagemDoOutro"), null);
    }

    @Entao("a viagem deve registrar {decimal} kg de CO2 economizado")
    public void co2DaViagem(Double kg) {
        Response r = ctx.resposta();
        assertEquals(201, r.statusCode(), () -> "Viagem não registrada: " + r.asString());
        r.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/viagem-response.json"));
        assertEquals(kg, r.jsonPath().getDouble("co2EconomizadoKg"), TOLERANCIA, "CO2 economizado");
    }

    @E("devo ganhar {int} pontos")
    public void pontosDaViagem(int pontos) {
        assertEquals(pontos, ctx.resposta().jsonPath().getInt("pontosGanhos"), "Pontos da viagem");
    }

    @E("meu saldo de pontos deve ser {int}")
    public void saldo(int pontos) {
        Response r = api.preparar(ctx.token(), "GET", "/usuarios/" + ctx.valor("idUsuario"), null);
        ApiClient.exigir(r, 200, "consulta do saldo");
        assertEquals(pontos, r.jsonPath().getInt("pontosRecompensa"), "Saldo de pontos");
    }

    @Entao("meu saldo de pontos não deve ficar negativo")
    public void saldoNaoNegativo() {
        Response r = api.preparar(ctx.token(), "GET", "/usuarios/" + ctx.valor("idUsuario"), null);
        ApiClient.exigir(r, 200, "consulta do saldo");
        int saldo = r.jsonPath().getInt("pontosRecompensa");
        assertTrue(saldo >= 0, "Saldo de pontos ficou negativo: " + saldo);
    }

    @Entao("meu painel de impacto deve mostrar {int} viagens e {decimal} kg de CO2 economizado")
    public void painelDeImpacto(int viagens, Double kg) {
        Response r = api.preparar(ctx.token(), "GET",
                "/usuarios/" + ctx.valor("idUsuario") + "/impacto", null);
        ApiClient.exigir(r, 200, "consulta do impacto");
        r.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/impacto-response.json"));
        assertEquals(viagens, r.jsonPath().getInt("totalViagens"), "Total de viagens");
        assertEquals(kg, r.jsonPath().getDouble("co2TotalEconomizadoKg"), TOLERANCIA, "CO2 acumulado");
        // 1 árvore absorve ~22 kg de CO2 por ano (referência usada pela aplicação)
        assertEquals(kg / 22.0, r.jsonPath().getDouble("equivalenteArvoresPlantadas"), TOLERANCIA,
                "Equivalente em árvores");
    }

    // -------------------------------------------------------- recompensas

    @Quando("tento resgatar a recompensa {string}")
    public void resgatar(String recompensa) {
        long id = api.idPorNome("/recompensas", recompensa);
        api.executar("POST", "/recompensas/usuario/" + ctx.valor("idUsuario") + "/resgatar/" + id, null);
    }

    @Dado("que resgatei a recompensa {string}")
    public void resgatei(String recompensa) {
        long id = api.idPorNome("/recompensas", recompensa);
        Response r = api.preparar(ctx.token(), "POST",
                "/recompensas/usuario/" + ctx.valor("idUsuario") + "/resgatar/" + id, null);
        ApiClient.exigir(r, 200, "resgate de " + recompensa);
    }

    @Entao("o resgate deve ser recusado por falta de pontos")
    public void resgateRecusado() {
        Response r = ctx.resposta();
        assertEquals(422, r.statusCode(), () -> "Esperava recusa (422): " + r.asString());
        r.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/erro-padrao.json"));
        assertTrue(r.jsonPath().getString("mensagem").startsWith("Pontos insuficientes"),
                "Mensagem: " + r.jsonPath().getString("mensagem"));
    }

    @Entao("o resgate deve ser confirmado")
    public void resgateConfirmado() {
        Response r = ctx.resposta();
        assertEquals(200, r.statusCode(), () -> "Esperava resgate (200): " + r.asString());
        assertTrue(r.asString().contains("resgatada com sucesso"), "Corpo: " + r.asString());
    }

    // --------------------------------------------------------- governança

    @Quando("tento cadastrar um novo tipo de transporte")
    public void cadastrarModal() {
        String corpo = "{\"nome\":\"Modal nao autorizado " + ctx.valor("sufixo") + "\","
                + "\"emissaoCo2PorKm\":0.0,\"pontosPorKm\":50}";
        api.executar("POST", "/transportes", corpo);
    }

    @Entao("a operação deve ser negada por falta de permissão")
    public void negada() {
        Response r = ctx.resposta();
        assertEquals(403, r.statusCode(), () -> "Esperava 403: HTTP " + r.statusCode() + " " + r.asString());
    }

    @Quando("alguém tenta se cadastrar com o meu e-mail")
    public void cadastroDuplicado() {
        String corpo = "{\"nome\":\"Impostor\",\"email\":\"" + ctx.valor("emailUsuario")
                + "\",\"senha\":\"Outra@123\",\"tipo\":\"PESSOA_FISICA\"}";
        api.executar("POST", "/usuarios", corpo);
    }

    @Entao("o cadastro deve ser recusado informando que o e-mail já existe")
    public void emailDuplicado() {
        Response r = ctx.resposta();
        assertEquals(422, r.statusCode(), () -> "Esperava recusa (422): " + r.asString());
        r.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/erro-padrao.json"));
        assertTrue(r.jsonPath().getString("mensagem").contains(ctx.valor("emailUsuario")),
                "Mensagem: " + r.jsonPath().getString("mensagem"));
    }

    @Quando("tento entrar com a senha {string}")
    public void entrarComSenha(String senha) {
        login(ctx.valor("emailUsuario"), senha);
    }

    @Quando("tento entrar com a minha senha")
    public void entrarComMinhaSenha() {
        login(ctx.valor("emailUsuario"), ctx.valor("senhaUsuario"));
    }

    @Quando("tento entrar com um e-mail que não existe")
    public void entrarComEmailInexistente() {
        login("ninguem." + ctx.valor("sufixo") + "@teste.com", ApiClient.SENHA_PADRAO);
    }

    @Quando("desativo a minha conta")
    public void desativarConta() {
        Response r = api.preparar(ctx.token(), "DELETE", "/usuarios/" + ctx.valor("idUsuario"), null);
        ApiClient.exigir(r, 204, "desativação da conta");
    }

    @Entao("o acesso deve ser negado com a mensagem {string}")
    public void acessoNegado(String mensagem) {
        Response r = ctx.resposta();
        assertEquals(422, r.statusCode(), () -> "Esperava recusa (422): " + r.asString());
        r.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/erro-padrao.json"));
        assertEquals(mensagem, r.jsonPath().getString("mensagem"));
    }

    // ------------------------------------------------------------- apoio

    private void login(String email, String senha) {
        api.executar("POST", "/auth/login",
                "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}");
    }

    private static String corpoViagem(long idModal, double km) {
        return String.format(Locale.ROOT,
                "{\"idTipoTransporte\":%d,\"origem\":\"Av. Paulista\",\"destino\":\"Pinheiros\","
                        + "\"distanciaKm\":%s,\"descricao\":\"Viagem registrada pela suite BDD\"}",
                idModal, km);
    }
}
