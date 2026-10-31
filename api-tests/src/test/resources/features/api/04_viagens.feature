# language: pt
@api
Funcionalidade: API de viagens
  Endpoints de /viagens: registro, consulta, histórico, atualização e exclusão

  Contexto:
    Dado que estou autenticado como um novo usuário comum
    E guardo o id do transporte "Bicicleta" como "idBicicleta"

  Cenário: Registrar viagem devolve 201 com o crédito calculado
    Quando faço uma requisição POST em "/viagens/usuario/{{idUsuario}}" com o corpo:
      """
      {"idTipoTransporte": {{idBicicleta}}, "origem": "Av. Paulista", "destino": "Pinheiros", "distanciaKm": 4.0, "descricao": "Ida ao trabalho"}
      """
    Então o status da resposta deve ser 201
    E a resposta deve seguir o contrato "viagem-response.json"
    E o campo "co2EconomizadoKg" deve valer 0,84
    E o campo "pontosGanhos" deve valer 40
    E o campo "tipoTransporte" deve ser "Bicicleta"
    E guardo o campo "id" como "idCriada"
    E o cabeçalho Location deve terminar com "/viagens/{{idCriada}}"

  Cenário: Consultar viagem por ID
    Dado que registrei uma viagem de 3 km usando "Caminhada"
    Quando faço uma requisição GET em "/viagens/{{idViagem}}"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "viagem-response.json"
    E o campo "id" deve ser "{{idViagem}}"
    E o campo "tipoTransporte" deve ser "Caminhada"

  Cenário: Histórico lista as viagens do usuário
    Dado que registrei uma viagem de 3 km usando "Caminhada"
    E que registrei uma viagem de 7 km usando "Bicicleta"
    Quando faço uma requisição GET em "/viagens/usuario/{{idUsuario}}"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "viagem-lista.json"
    E a resposta deve ser uma lista com pelo menos 2 itens

  Cenário: Atualizar viagem recalcula CO2 e pontos
    Dado que registrei uma viagem de 10 km usando "Bicicleta"
    E guardo o id do transporte "Carro Híbrido" como "idHibrido"
    Quando faço uma requisição PUT em "/viagens/{{idViagem}}" com o corpo:
      """
      {"idTipoTransporte": {{idHibrido}}, "origem": "Av. Paulista", "destino": "Pinheiros", "distanciaKm": 10.0, "descricao": "Corrigido: foi de carro hibrido"}
      """
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "viagem-response.json"
    E o campo "co2EconomizadoKg" deve valer 0,90
    E o campo "pontosGanhos" deve valer 20

  Cenário: Atualizar viagem inexistente devolve 404
    Quando faço uma requisição PUT em "/viagens/999999999" com o corpo:
      """
      {"idTipoTransporte": {{idBicicleta}}, "origem": "Av. Paulista", "destino": "Pinheiros", "distanciaKm": 3.0}
      """
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro-padrao.json"

  Cenário: Excluir viagem
    Dado que registrei uma viagem de 2 km usando "Bicicleta"
    Quando faço uma requisição DELETE em "/viagens/{{idViagem}}"
    Então o status da resposta deve ser 204
    Quando faço uma requisição GET em "/viagens/{{idViagem}}"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro-padrao.json"

  Cenário: Viagem com distância inválida é recusada
    Quando faço uma requisição POST em "/viagens/usuario/{{idUsuario}}" com o corpo:
      """
      {"idTipoTransporte": {{idBicicleta}}, "origem": "", "destino": "Pinheiros", "distanciaKm": -5}
      """
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro-validacao.json"
    E o erro de validação deve apontar o campo "distanciaKm"
    E o erro de validação deve apontar o campo "origem"

  Cenário: Viagem com modal inexistente devolve 404
    Quando faço uma requisição POST em "/viagens/usuario/{{idUsuario}}" com o corpo:
      """
      {"idTipoTransporte": 999999999, "origem": "Av. Paulista", "destino": "Pinheiros", "distanciaKm": 3.0}
      """
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro-padrao.json"

  Cenário: Buscar viagem inexistente devolve 404
    Quando faço uma requisição GET em "/viagens/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro-padrao.json"

  Cenário: Excluir viagem inexistente devolve 404
    Quando faço uma requisição DELETE em "/viagens/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro-padrao.json"

  Cenário: Histórico de usuário inexistente devolve 404
    Quando faço uma requisição GET em "/viagens/usuario/999999999"
    Então o status da resposta deve ser 404
    E a resposta deve seguir o contrato "erro-padrao.json"
