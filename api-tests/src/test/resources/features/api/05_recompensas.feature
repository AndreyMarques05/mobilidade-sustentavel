# language: pt
@api
Funcionalidade: API de recompensas
  Endpoints de /recompensas: consulta, cadastro e desativação pelo administrador
  e resgate com pontos

  Cenário: Listar recompensas ativas da mais barata para a mais cara
    Dado que estou autenticado como um novo usuário comum
    Quando faço uma requisição GET em "/recompensas"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "recompensa-lista.json"
    E a resposta deve ser uma lista com pelo menos 4 itens
    E a lista deve estar ordenada pelo campo "pontosNecessarios" em ordem crescente

  Cenário: Buscar recompensa por ID
    Dado que estou autenticado como um novo usuário comum
    E guardo o id da recompensa "Desconto 10% em Bike Share" como "idBike"
    Quando faço uma requisição GET em "/recompensas/{{idBike}}"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "recompensa-response.json"
    E o campo "pontosNecessarios" deve valer 100

  Cenário: Buscar recompensa inexistente devolve 404
    Dado que estou autenticado como um novo usuário comum
    Quando faço uma requisição GET em "/recompensas/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro-padrao.json"

  Cenário: Administrador cadastra uma recompensa
    Dado que estou autenticado como administrador
    Quando faço uma requisição POST em "/recompensas" com o corpo:
      """
      {"nome": "Vale BDD {{sufixo}}", "descricao": "Criada pelo teste automatizado", "pontosNecessarios": 300, "quantidade": 10}
      """
    Então o status da resposta deve ser 201
    E a resposta deve seguir o contrato "recompensa-response.json"
    E o campo "ativo" deve ser verdadeiro
    E guardo o campo "id" como "idCriada"
    E o cabeçalho Location deve terminar com "/recompensas/{{idCriada}}"

  Cenário: Recompensa com dados inválidos aponta cada campo com erro
    Dado que estou autenticado como administrador
    Quando faço uma requisição POST em "/recompensas" com o corpo:
      """
      {"nome": "", "pontosNecessarios": 0, "quantidade": -1}
      """
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro-validacao.json"
    E o erro de validação deve apontar o campo "nome"
    E o erro de validação deve apontar o campo "pontosNecessarios"
    E o erro de validação deve apontar o campo "quantidade"

  Cenário: Usuário comum não pode cadastrar recompensas
    Dado que estou autenticado como um novo usuário comum
    Quando faço uma requisição POST em "/recompensas" com o corpo:
      """
      {"nome": "Golpe {{sufixo}}", "pontosNecessarios": 1, "quantidade": 999}
      """
    Então o status da resposta deve ser 403

  Cenário: Usuário comum não pode desativar recompensas
    Dado que existe uma recompensa criada pelo administrador
    E que estou autenticado como um novo usuário comum
    Quando faço uma requisição DELETE em "/recompensas/{{idRecompensa}}"
    Então o status da resposta deve ser 403

  Cenário: Recompensa desativada deixa de poder ser resgatada
    Dado que existe uma recompensa criada pelo administrador
    E que estou autenticado como administrador
    Quando faço uma requisição DELETE em "/recompensas/{{idRecompensa}}"
    Então o status da resposta deve ser 204
    Dado que estou autenticado como um novo usuário comum
    Quando faço uma requisição POST em "/recompensas/usuario/{{idUsuario}}/resgatar/{{idRecompensa}}"
    Então o status da resposta deve ser 422
    E a resposta deve seguir o contrato "erro-padrao.json"

  Cenário: Resgate com saldo suficiente debita os pontos
    Dado que estou autenticado como um novo usuário comum
    E que registrei uma viagem de 2 km usando "Bicicleta"
    E que existe uma recompensa criada pelo administrador
    Quando faço uma requisição POST em "/recompensas/usuario/{{idUsuario}}/resgatar/{{idRecompensa}}"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve conter "resgatada com sucesso"
    E o corpo da resposta deve conter "Pontos restantes: 10"
