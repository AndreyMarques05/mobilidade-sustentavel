package br.com.fiap.mobilidade.apitests.steps;

import br.com.fiap.mobilidade.apitests.support.ApiClient;
import br.com.fiap.mobilidade.apitests.support.Contexto;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;

import java.nio.charset.StandardCharsets;

/**
 * Ganchos executados ao final de cada cenário.
 */
public class Hooks {

    private final Contexto ctx;
    private final ApiClient api;

    public Hooks(Contexto ctx, ApiClient api) {
        this.ctx = ctx;
        this.api = api;
    }

    /**
     * Anexa ao relatório a última chamada do cenário e a resposta da API.
     * É a evidência de execução: o relatório HTML mostra, cenário por cenário,
     * o que foi enviado e o que voltou.
     */
    @After
    public void anexarEvidencia(Scenario cenario) {
        ctx.respostaOpcional().ifPresent(r -> {
            String corpo = r.asString();
            String evidencia = ctx.ultimaChamada()
                    + "\n\n-> HTTP " + r.statusCode()
                    + (corpo == null || corpo.isBlank() ? "" : "\n" + r.asPrettyString());
            cenario.attach(evidencia.getBytes(StandardCharsets.UTF_8), "text/plain",
                    "Última chamada à API");
        });
    }

    /**
     * Alguns defeitos conhecidos deixam o usuário do cenário em estado inválido
     * (ex.: saldo de pontos negativo). Para não contaminar o banco compartilhado,
     * esse usuário é desativado no fim do cenário: usuários inativos não aparecem
     * em GET /usuarios nem no ranking, cujos contratos exigem pontos >= 0.
     */
    @After("@desativa-usuario-ao-final")
    public void desativarUsuarioDoCenario() {
        ctx.valorOpcional("idUsuario").ifPresent(id ->
                api.preparar(api.tokenAdmin(), "DELETE", "/usuarios/" + id, null));
    }
}
