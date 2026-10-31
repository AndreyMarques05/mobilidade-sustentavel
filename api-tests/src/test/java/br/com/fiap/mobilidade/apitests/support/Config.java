package br.com.fiap.mobilidade.apitests.support;

/**
 * Configuração da execução. Cada valor pode vir de uma propriedade de sistema
 * (-Dapi.baseUrl=...) ou de uma variável de ambiente (API_BASE_URL=...).
 * Sem nenhuma das duas, vale o padrão do ambiente de staging local.
 */
public final class Config {

    private Config() {
    }

    /** URL da API sob teste. Padrão: staging do docker-compose (porta 8081). */
    public static String baseUrl() {
        return valor("api.baseUrl", "API_BASE_URL", "http://localhost:8081");
    }

    /** Administrador criado pela migration V2 e ajustado pelo DataInitializer da aplicação. */
    public static String adminEmail() {
        return valor("api.adminEmail", "API_ADMIN_EMAIL", "admin@mobilidade.com");
    }

    public static String adminSenha() {
        return valor("api.adminSenha", "API_ADMIN_SENHA", "admin123");
    }

    private static String valor(String propriedade, String variavel, String padrao) {
        String v = System.getProperty(propriedade);
        if (v == null || v.isBlank() || v.startsWith("${")) {
            v = System.getenv(variavel);
        }
        return (v == null || v.isBlank()) ? padrao : v.trim();
    }
}
