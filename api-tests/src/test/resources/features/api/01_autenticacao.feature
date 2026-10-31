# language: pt
@api
Funcionalidade: API de autenticação
  Contrato e códigos de status de POST /auth/login e do health check público

  Cenário: Login do administrador devolve um token JWT
    Quando faço uma requisição POST em "/auth/login" com o corpo:
      """
      {"email": "{{adminEmail}}", "senha": "{{adminSenha}}"}
      """
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "login-response.json"
    E o campo "tipo" deve ser "Bearer"
    E o campo "role" deve ser "ADMIN"

  Cenário: Login com credenciais inválidas devolve erro de negócio
    Quando faço uma requisição POST em "/auth/login" com o corpo:
      """
      {"email": "nao-existe.{{sufixo}}@teste.com", "senha": "errada"}
      """
    Então o status da resposta deve ser 422
    E a resposta deve seguir o contrato "erro-padrao.json"

  Cenário: Login com dados malformados devolve erro de validação
    Quando faço uma requisição POST em "/auth/login" com o corpo:
      """
      {"email": "isto-nao-e-email", "senha": ""}
      """
    Então o status da resposta deve ser 400
    E a resposta deve seguir o contrato "erro-validacao.json"
    E o erro de validação deve apontar o campo "email"
    E o erro de validação deve apontar o campo "senha"

  Cenário: Health check é público e responde UP
    Dado que não estou autenticado
    Quando faço uma requisição GET em "/actuator/health"
    Então o status da resposta deve ser 200
    E a resposta deve seguir o contrato "health.json"
