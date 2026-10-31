package br.com.fiap.mobilidade.apitests.steps;

import io.cucumber.java.ParameterType;

/**
 * Tipos de parâmetro usados nos passos.
 *
 * {decimal} aceita vírgula ou ponto ("12,5" ou "12.5"). Os cenários estão em
 * português e usam vírgula; o {double} nativo do Cucumber interpreta números
 * conforme o idioma do arquivo, o que torna "12.5" ambíguo em pt.
 */
public class Tipos {

    @ParameterType("-?\\d+(?:[.,]\\d+)?")
    public Double decimal(String valor) {
        return Double.valueOf(valor.replace(',', '.'));
    }
}
