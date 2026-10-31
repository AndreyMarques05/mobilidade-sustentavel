# ==============================================================================
# Sobe o ambiente de staging (API + Oracle), espera ficar saudavel e roda a
# suite de testes de API e BDD contra ele. Versao Windows / PowerShell.
#
#   .\run-tests.ps1              # sobe o staging e roda tudo
#   .\run-tests.ps1 -SemSubir    # usa uma API que ja esta no ar
#
# Se java/mvn nao estiverem no PATH, informe -JavaHome e -Mvn.
# ==============================================================================
param(
    [switch]$SemSubir,
    [string]$BaseUrl = $(if ($env:API_BASE_URL) { $env:API_BASE_URL } else { "http://localhost:8081" }),
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$Mvn = "mvn"
)

Set-Location $PSScriptRoot
$env:API_BASE_URL = $BaseUrl
if ($JavaHome) { $env:JAVA_HOME = $JavaHome }

if (-not $SemSubir) {
    Write-Host ">> Subindo o ambiente de staging..."
    # O docker compose escreve o progresso no stderr; o que vale e o codigo de saida.
    docker compose -f ..\docker-compose.staging.yml up -d
    if ($LASTEXITCODE -ne 0) { throw "Falha ao subir o staging com docker compose." }
}

Write-Host ">> Aguardando $BaseUrl/actuator/health responder UP..."
$ok = $false
for ($i = 1; $i -le 60; $i++) {
    try {
        $saude = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -TimeoutSec 5 -ErrorAction Stop
        if ($saude.status -eq "UP") { Write-Host ">> API no ar apos $(($i - 1) * 5)s de espera."; $ok = $true; break }
    } catch { }
    Start-Sleep -Seconds 5
}
if (-not $ok) { throw "A API nao ficou saudavel em 5 minutos." }

Write-Host ">> Rodando os testes..."
& $Mvn -B test
$codigo = $LASTEXITCODE

Write-Host ">> Relatorio: $PSScriptRoot\target\cucumber-report\index.html"
exit $codigo
