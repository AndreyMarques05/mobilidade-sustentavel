# Evidências de execução

Prints reais do pipeline no GitHub Actions e dos ambientes rodando localmente,
capturados em 12-13/10/2026. Também estão na documentação técnica em PDF.

| Arquivo | Evidência |
|---|---|
| 01-pipeline-completo.png | Execução #4: os quatro jobs concluídos com sucesso |
| 02-testes.png | Etapa de testes: 19 testes, 0 falhas, BUILD SUCCESS |
| 03-imagem-publicada.png | Imagem no GHCR, com as tags production/latest aplicadas após a aprovação |
| 04-deploy-staging.png | Deploy em staging: health check UP e smoke test |
| 05-deploy-producao.png | Portão de aprovação manual e deploy em produção |
| 06-ambientes-no-ar.png | Staging e produção lado a lado: docker ps + health checks |
| 07-aplicacao-funcionando.png | Swagger e login em staging; produção com Swagger desligado (404) |

Histórico completo: https://github.com/AndreyMarques05/mobilidade-sustentavel/actions
