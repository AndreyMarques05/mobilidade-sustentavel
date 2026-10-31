package br.com.fiap.mobilidade.apitests.steps;

import br.com.fiap.mobilidade.apitests.support.ApiClient;
import br.com.fiap.mobilidade.apitests.support.Contexto;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Passos técnicos, de nível HTTP. Usados nos arquivos de features/api para
 * cobrir todos os endpoints: status code, corpo da resposta e contrato
 * (JSON Schema).
 */
public class ApiSteps {

    private static final double TOLERANCIA = 0.0001;

    private final Contexto ctx;
    private final ApiClient api;

    public ApiSteps(Contexto ctx, ApiClient api) {
        this.ctx = ctx;
        this.api = api;
    }

    // ---------------------------------------------------------------- ações

    @Quando("faço uma requisição {word} em {string}")
    public void requisicao(String metodo, String caminho) {
        api.executar(metodo, ctx.resolver(caminho), null);
    }

    @Quando("faço uma requisição {word} em {string} com o corpo:")
    public void requisicaoComCorpo(String metodo, String caminho, String corpo) {
        api.executar(metodo, ctx.resolver(caminho), ctx.resolver(corpo));
    }

    // ---------------------------------------------------------- verificações

    @Entao("o status da resposta deve ser {int}")
    public void status(int esperado) {
        Response r = ctx.resposta();
        assertEquals(esperado, r.statusCode(), () -> "Status inesperado.\nChamada: "
                + ctx.ultimaChamada() + "\nResposta: " + r.asString());
    }

    @E("a resposta deve seguir o contrato {string}")
    public void contrato(String schema) {
        ctx.resposta().then().assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/" + schema));
    }

    @E("o campo {string} deve ser {string}")
    public void campoIgual(String campo, String esperado) {
        Object valor = ctx.resposta().jsonPath().get(campo);
        assertNotNull(valor, "Campo ausente na resposta: " + campo);
        assertEquals(ctx.resolver(esperado), String.valueOf(valor), "Campo " + campo);
    }

    @E("o campo {string} deve valer {decimal}")
    public void campoNumerico(String campo, Double esperado) {
        Object valor = ctx.resposta().jsonPath().get(campo);
        assertNotNull(valor, "Campo ausente na resposta: " + campo);
        assertEquals(esperado, ((Number) valor).doubleValue(), TOLERANCIA, "Campo " + campo);
    }

    @E("o campo {string} deve ser verdadeiro")
    public void campoVerdadeiro(String campo) {
        assertEquals(Boolean.TRUE, ctx.resposta().jsonPath().get(campo), "Campo " + campo);
    }

    @E("o campo {string} deve ser falso")
    public void campoFalso(String campo) {
        assertEquals(Boolean.FALSE, ctx.resposta().jsonPath().get(campo), "Campo " + campo);
    }

    @E("o campo {string} deve estar preenchido")
    public void campoPreenchido(String campo) {
        Object valor = ctx.resposta().jsonPath().get(campo);
        assertNotNull(valor, "Campo ausente na resposta: " + campo);
        assertFalse(String.valueOf(valor).isBlank(), "Campo vazio: " + campo);
    }

    @E("a resposta deve ser uma lista com pelo menos {int} itens")
    public void listaComPeloMenos(int minimo) {
        List<Object> itens = ctx.resposta().jsonPath().getList("$");
        assertTrue(itens.size() >= minimo,
                "Esperava ao menos " + minimo + " itens, vieram " + itens.size());
    }

    @E("a lista deve estar ordenada pelo campo {string} em ordem decrescente")
    public void ordenadaDecrescente(String campo) {
        verificarOrdem(campo, false);
    }

    @E("a lista deve estar ordenada pelo campo {string} em ordem crescente")
    public void ordenadaCrescente(String campo) {
        verificarOrdem(campo, true);
    }

    @E("a mensagem de erro deve conter {string}")
    public void mensagemContem(String trecho) {
        String mensagem = ctx.resposta().jsonPath().getString("mensagem");
        assertNotNull(mensagem, "Resposta sem campo 'mensagem': " + ctx.resposta().asString());
        String esperado = ctx.resolver(trecho);
        assertTrue(mensagem.contains(esperado),
                "A mensagem '" + mensagem + "' não contém '" + esperado + "'");
    }

    @E("o erro de validação deve apontar o campo {string}")
    public void erroDeValidacao(String campo) {
        Object erro = ctx.resposta().jsonPath().get("campos." + campo);
        assertNotNull(erro, "A validação não apontou o campo '" + campo + "': "
                + ctx.resposta().asString());
    }

    @E("o corpo da resposta deve conter {string}")
    public void corpoContem(String trecho) {
        String esperado = ctx.resolver(trecho);
        String corpo = ctx.resposta().asString();
        assertTrue(corpo.contains(esperado), "O corpo '" + corpo + "' não contém '" + esperado + "'");
    }

    @E("o cabeçalho Location deve terminar com {string}")
    public void location(String fim) {
        String location = ctx.resposta().header("Location");
        assertNotNull(location, "Resposta sem cabeçalho Location");
        String esperado = ctx.resolver(fim);
        assertTrue(location.endsWith(esperado),
                "Location '" + location + "' não termina com '" + esperado + "'");
    }

    @E("guardo o campo {string} como {string}")
    public void guardarCampo(String campo, String variavel) {
        Object valor = ctx.resposta().jsonPath().get(campo);
        assertNotNull(valor, "Campo ausente na resposta: " + campo);
        if (valor instanceof Number n && n.doubleValue() == Math.rint(n.doubleValue())) {
            valor = n.longValue();
        }
        ctx.guardar(variavel, valor);
    }

    private void verificarOrdem(String campo, boolean crescente) {
        List<Number> valores = ctx.resposta().jsonPath().getList(campo);
        for (int i = 1; i < valores.size(); i++) {
            double anterior = valores.get(i - 1).doubleValue();
            double atual = valores.get(i).doubleValue();
            boolean ok = crescente ? anterior <= atual : anterior >= atual;
            assertTrue(ok, "Lista fora de ordem no campo " + campo + " na posição " + i
                    + ": " + anterior + " seguido de " + atual);
        }
    }
}
