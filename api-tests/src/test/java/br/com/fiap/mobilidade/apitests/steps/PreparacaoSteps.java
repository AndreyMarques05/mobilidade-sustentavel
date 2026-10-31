package br.com.fiap.mobilidade.apitests.steps;

import br.com.fiap.mobilidade.apitests.support.ApiClient;
import br.com.fiap.mobilidade.apitests.support.Contexto;
import io.cucumber.java.pt.Dado;
import io.restassured.response.Response;

import java.util.Map;

/**
 * Passos "Dado": quem está autenticado e quais dados existem antes da ação.
 * Toda a preparação usa ApiClient.preparar(), que não substitui "a resposta"
 * avaliada pelos passos "Então".
 */
public class PreparacaoSteps {

    private final Contexto ctx;
    private final ApiClient api;

    public PreparacaoSteps(Contexto ctx, ApiClient api) {
        this.ctx = ctx;
        this.api = api;
    }

    @Dado("que estou autenticado como administrador")
    public void comoAdministrador() {
        ctx.usarToken(api.tokenAdmin());
    }

    @Dado("que estou autenticado como um novo usuário comum")
    public void comoNovoUsuario() {
        Map<String, String> u = api.novoUsuarioAutenticado();
        ctx.usarToken(u.get("token"));
        ctx.guardar("idUsuario", u.get("id"));
        ctx.guardar("emailUsuario", u.get("email"));
        ctx.guardar("senhaUsuario", u.get("senha"));
    }

    @Dado("que sou um usuário recém-cadastrado, ainda sem pontos")
    public void usuarioSemPontos() {
        comoNovoUsuario();
    }

    @Dado("que não estou autenticado")
    public void semToken() {
        ctx.usarToken(null);
    }

    @Dado("guardo o id do transporte {string} como {string}")
    public void idDoTransporte(String nome, String variavel) {
        ctx.guardar(variavel, api.idPorNome("/transportes", nome));
    }

    @Dado("guardo o id da recompensa {string} como {string}")
    public void idDaRecompensa(String nome, String variavel) {
        ctx.guardar(variavel, api.idPorNome("/recompensas", nome));
    }

    @Dado("que existe um modal criado pelo administrador")
    public void modalCriadoPeloAdmin() {
        String corpo = "{\"nome\":\"Modal BDD " + ctx.valor("sufixo") + "\","
                + "\"descricao\":\"Criado pela suite de testes\","
                + "\"emissaoCo2PorKm\":0.01,\"pontosPorKm\":6}";
        Response r = api.preparar(api.tokenAdmin(), "POST", "/transportes", corpo);
        ApiClient.exigir(r, 201, "criação do modal de teste");
        ctx.guardar("idTransporte", r.jsonPath().getLong("id"));
    }

    @Dado("que existe uma recompensa criada pelo administrador")
    public void recompensaCriadaPeloAdmin() {
        String corpo = "{\"nome\":\"Recompensa BDD " + ctx.valor("sufixo") + "\","
                + "\"descricao\":\"Criada pela suite de testes\","
                + "\"pontosNecessarios\":10,\"quantidade\":5}";
        Response r = api.preparar(api.tokenAdmin(), "POST", "/recompensas", corpo);
        ApiClient.exigir(r, 201, "criação da recompensa de teste");
        ctx.guardar("idRecompensa", r.jsonPath().getLong("id"));
    }
}
