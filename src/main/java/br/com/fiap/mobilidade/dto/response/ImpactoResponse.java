package br.com.fiap.mobilidade.dto.response;

public record ImpactoResponse(
        Long idUsuario,
        String nomeUsuario,
        Integer totalViagens,
        Double distanciaTotalKm,
        Double co2TotalEconomizadoKg,
        Double equivalenteArvoresPlantadas,
        Integer pontosAcumulados
) {
    /** 1 árvore absorve ~22 kg CO2/ano; usamos como referência anual. */
    public static double calcularArvores(double co2Kg) {
        return co2Kg / 22.0;
    }
}
