# language: pt
@api
Funcionalidade: API de tipos de transporte
  Endpoints de /transportes: consulta para qualquer usuário autenticado
  e manutenção do catálogo restrita ao administrador

  Cenário: Listar os modais ativos
    Dado que estou autenticado como um novo usuário comum
    Quando faço uma requisição GET em "/transportes"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "transporte-lista.json"
    E a resposta deve ser uma lista com pelo menos 8 itens

  Cenário: Buscar um modal por ID
    Dado que estou autenticado como um novo usuário comum
    E guardo o id do transporte "Bicicleta" como "idBicicleta"
    Quando faço uma requisição GET em "/transportes/{{idBicicleta}}"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "transporte-response.json"
    E o campo "nome" deve ser "Bicicleta"
    E o campo "emissaoCo2PorKm" deve valer 0
    E o campo "pontosPorKm" deve valer 10

  Cenário: Buscar modal inexistente devolve 404
    Dado que estou autenticado como um novo usuário comum
    Quando faço uma requisição GET em "/transportes/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro-padrao.json"

  Cenário: Administrador cadastra um novo modal
    Dado que estou autenticado como administrador
    Quando faço uma requisição POST em "/transportes" com o corpo:
      """
      {"nome": "Bonde BDD {{sufixo}}", "descricao": "Criado pelo teste automatizado", "emissaoCo2PorKm": 0.01, "pontosPorKm": 6}
      """
    Então o status da resposta deve ser 201
    E a resposta deve seguir o contrato "transporte-response.json"
    E o campo "ativo" deve ser verdadeiro
    E guardo o campo "id" como "idCriado"
    E o cabeçalho Location deve terminar com "/transportes/{{idCriado}}"

  Cenário: Administrador altera um modal
    Dado que existe um modal criado pelo administrador
    E que estou autenticado como administrador
    Quando faço uma requisição PUT em "/transportes/{{idTransporte}}" com o corpo:
      """
      {"nome": "Modal BDD {{sufixo}}", "descricao": "Atualizado pelo teste", "emissaoCo2PorKm": 0.02, "pontosPorKm": 5}
      """
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "transporte-response.json"
    E o campo "pontosPorKm" deve valer 5
    E o campo "emissaoCo2PorKm" deve valer 0,02

  Cenário: Alterar modal inexistente devolve 404
    Dado que estou autenticado como administrador
    Quando faço uma requisição PUT em "/transportes/999999999" com o corpo:
      """
      {"nome": "Fantasma {{sufixo}}", "emissaoCo2PorKm": 0.0, "pontosPorKm": 1}
      """
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro-padrao.json"

  Cenário: Administrador desativa um modal
    Dado que existe um modal criado pelo administrador
    E que estou autenticado como administrador
    Quando faço uma requisição DELETE em "/transportes/{{idTransporte}}"
    Então o status da resposta deve ser 204
    Quando faço uma requisição GET em "/transportes/{{idTransporte}}"
    Então o status da resposta deve ser 200
    E o campo "ativo" deve ser falso

  Cenário: Modal com nome repetido é recusado
    Dado que estou autenticado como administrador
    Quando faço uma requisição POST em "/transportes" com o corpo:
      """
      {"nome": "Bicicleta", "emissaoCo2PorKm": 0.0, "pontosPorKm": 10}
      """
    Então o status da resposta deve ser 422
    E a resposta deve seguir o contrato "erro-padrao.json"
    E a mensagem de erro deve conter "Bicicleta"

  Cenário: Modal com dados inválidos aponta cada campo com erro
    Dado que estou autenticado como administrador
    Quando faço uma requisição POST em "/transportes" com o corpo:
      """
      {"nome": "", "emissaoCo2PorKm": -1, "pontosPorKm": -5}
      """
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro-validacao.json"
    E o erro de validação deve apontar o campo "nome"
    E o erro de validação deve apontar o campo "emissaoCo2PorKm"
    E o erro de validação deve apontar o campo "pontosPorKm"

  Cenário: Usuário comum não pode alterar nem desativar modais
    Dado que existe um modal criado pelo administrador
    E que estou autenticado como um novo usuário comum
    Quando faço uma requisição PUT em "/transportes/{{idTransporte}}" com o corpo:
      """
      {"nome": "Tentativa {{sufixo}}", "emissaoCo2PorKm": 0.0, "pontosPorKm": 99}
      """
    Então o status da resposta deve ser 403
    Quando faço uma requisição DELETE em "/transportes/{{idTransporte}}"
    Então o status da resposta deve ser 403
