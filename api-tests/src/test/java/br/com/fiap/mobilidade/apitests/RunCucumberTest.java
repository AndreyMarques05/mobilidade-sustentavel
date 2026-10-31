package br.com.fiap.mobilidade.apitests;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Ponto de entrada da suíte: o Maven Surefire roda esta classe e ela entrega
 * todos os arquivos .feature de src/test/resources/features ao Cucumber.
 *
 * Glue, relatórios e filtro de tags ficam em junit-platform.properties.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
public class RunCucumberTest {
}
