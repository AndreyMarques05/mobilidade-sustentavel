# language: pt
@negocio @esg-governanca
Funcionalidade: Controle de acesso e integridade dos cadastros
  Como responsável pela governança da plataforma
  Quero que só administradores alterem o catálogo e que os cadastros sejam íntegros
  Para que pontos e créditos de CO2 sejam confiáveis

  Cenário: Usuário comum não pode cadastrar tipos de transporte
    Dado que estou autenticado como um novo usuário comum
    Quando tento cadastrar um novo tipo de transporte
    Então a operação deve ser negada por falta de permissão

  Cenário: Cadastro com e-mail já existente é recusado
    Dado que sou um usuário recém-cadastrado, ainda sem pontos
    Quando alguém tenta se cadastrar com o meu e-mail
    Então o cadastro deve ser recusado informando que o e-mail já existe

  Cenário: Login inválido não revela se o e-mail existe
    Dado que sou um usuário recém-cadastrado, ainda sem pontos
    Quando tento entrar com a senha "senha-errada"
    Então o acesso deve ser negado com a mensagem "Credenciais inválidas."
    Quando tento entrar com um e-mail que não existe
    Então o acesso deve ser negado com a mensagem "Credenciais inválidas."

  Cenário: Conta desativada não consegue mais entrar
    Dado que sou um usuário recém-cadastrado, ainda sem pontos
    Quando desativo a minha conta
    E tento entrar com a minha senha
    Então o acesso deve ser negado com a mensagem "Credenciais inválidas."

  @defeito-conhecido
  Cenário: Usuário não pode excluir a viagem de outro usuário
    Dado que outro usuário registrou uma viagem de 5 km usando "Bicicleta"
    E que estou autenticado como um novo usuário comum
    Quando tento excluir a viagem do outro usuário
    Então a operação deve ser negada por falta de permissão

  @defeito-conhecido @desativa-usuario-ao-final
  Cenário: Corrigir uma viagem cujos pontos já foram gastos não deixa o saldo negativo
    Dado que sou um usuário recém-cadastrado, ainda sem pontos
    E que registrei uma viagem de 15 km usando "Bicicleta"
    E que resgatei a recompensa "Desconto 10% em Bike Share"
    Quando corrijo a viagem para 15 km usando "Carro Híbrido"
    Então meu saldo de pontos não deve ficar negativo
