# language: pt
@api
Funcionalidade: API de usuários
  Endpoints de /usuarios: cadastro, consulta, listagem, impacto, ranking,
  resgate de recompensa e desativação

  Cenário: Cadastro público de usuário devolve 201 e o recurso criado
    Dado que não estou autenticado
    Quando faço uma requisição POST em "/usuarios" com o corpo:
      """
      {"nome": "Empresa API {{sufixo}}", "email": "api.{{sufixo}}@teste.com", "senha": "Senha@123", "tipo": "EMPRESA"}
      """
    Então o status da resposta deve ser 201
    E a resposta deve seguir o contrato "usuario-response.json"
    E o campo "role" deve ser "USER"
    E o campo "tipo" deve ser "EMPRESA"
    E o campo "pontosRecompensa" deve valer 0
    E guardo o campo "id" como "idNovo"
    E o cabeçalho Location deve terminar com "/usuarios/{{idNovo}}"

  Cenário: Cadastro com dados inválidos aponta cada campo com erro
    Dado que não estou autenticado
    Quando faço uma requisição POST em "/usuarios" com o corpo:
      """
      {"nome": "A", "email": "invalido", "senha": "123", "tipo": null}
      """
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro-validacao.json"
    E o erro de validação deve apontar o campo "nome"
    E o erro de validação deve apontar o campo "email"
    E o erro de validação deve apontar o campo "senha"
    E o erro de validação deve apontar o campo "tipo"

  Cenário: Buscar usuário por ID
    Dado que estou autenticado como um novo usuário comum
    Quando faço uma requisição GET em "/usuarios/{{idUsuario}}"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "usuario-response.json"
    E o campo "id" deve ser "{{idUsuario}}"
    E o campo "ativo" deve ser verdadeiro

  Cenário: Buscar usuário inexistente devolve 404
    Dado que estou autenticado como um novo usuário comum
    Quando faço uma requisição GET em "/usuarios/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro-padrao.json"

  Cenário: Administrador lista os usuários ativos
    Dado que estou autenticado como administrador
    Quando faço uma requisição GET em "/usuarios"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "usuario-lista.json"
    E a resposta deve ser uma lista com pelo menos 1 itens

  Cenário: Usuário comum não pode listar todos os usuários
    Dado que estou autenticado como um novo usuário comum
    Quando faço uma requisição GET em "/usuarios"
    Então o status da resposta deve ser 403

  Cenário: Requisição sem token é recusada
    Dado que não estou autenticado
    Quando faço uma requisição GET em "/usuarios/1"
    Então o status da resposta deve ser 403

  Cenário: Impacto de um usuário sem viagens começa zerado
    Dado que estou autenticado como um novo usuário comum
    Quando faço uma requisição GET em "/usuarios/{{idUsuario}}/impacto"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "impacto-response.json"
    E o campo "totalViagens" deve valer 0
    E o campo "co2TotalEconomizadoKg" deve valer 0
    E o campo "pontosAcumulados" deve valer 0

  Cenário: Impacto de usuário inexistente devolve 404
    Dado que estou autenticado como um novo usuário comum
    Quando faço uma requisição GET em "/usuarios/999999999/impacto"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro-padrao.json"

  Cenário: Ranking ordena os usuários do maior para o menor saldo
    Dado que estou autenticado como um novo usuário comum
    Quando faço uma requisição GET em "/usuarios/ranking"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "ranking-lista.json"
    E a resposta deve ser uma lista com pelo menos 1 itens
    E a lista deve estar ordenada pelo campo "pontosRecompensa" em ordem decrescente
    E a lista deve estar ordenada pelo campo "posicao" em ordem crescente

  Cenário: Resgate pela rota de usuários recusa saldo insuficiente
    Dado que estou autenticado como um novo usuário comum
    E guardo o id da recompensa "Desconto 10% em Bike Share" como "idBike"
    Quando faço uma requisição POST em "/usuarios/{{idUsuario}}/recompensas/{{idBike}}/resgatar"
    Então o status da resposta deve ser 422
    E a resposta deve seguir o contrato "erro-padrao.json"
    E a mensagem de erro deve conter "Pontos insuficientes"

  Cenário: Resgate pela rota de usuários debita os pontos quando há saldo
    Dado que estou autenticado como um novo usuário comum
    E que registrei uma viagem de 2 km usando "Bicicleta"
    E que existe uma recompensa criada pelo administrador
    Quando faço uma requisição POST em "/usuarios/{{idUsuario}}/recompensas/{{idRecompensa}}/resgatar"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve conter "resgatada com sucesso"
    E o corpo da resposta deve conter "Pontos restantes: 10"

  Cenário: Usuário desativa a própria conta
    Dado que estou autenticado como um novo usuário comum
    Quando faço uma requisição DELETE em "/usuarios/{{idUsuario}}"
    Então o status da resposta deve ser 204
    Dado que estou autenticado como administrador
    Quando faço uma requisição GET em "/usuarios/{{idUsuario}}"
    Então o status da resposta deve ser 200
    E o campo "ativo" deve ser falso

  Cenário: Desativar usuário inexistente devolve 404
    Dado que estou autenticado como administrador
    Quando faço uma requisição DELETE em "/usuarios/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro-padrao.json"
