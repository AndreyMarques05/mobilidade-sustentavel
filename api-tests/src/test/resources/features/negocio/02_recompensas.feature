# language: pt
@negocio @esg-social
Funcionalidade: Resgate de recompensas com pontos acumulados
  Como usuário engajado em mobilidade sustentável
  Quero trocar meus pontos por benefícios
  Para ser recompensado pela mudança de hábito

  Contexto:
    Dado que sou um usuário recém-cadastrado, ainda sem pontos

  Cenário: Resgate recusado quando o saldo de pontos não é suficiente
    Quando tento resgatar a recompensa "Desconto 10% em Bike Share"
    Então o resgate deve ser recusado por falta de pontos
    E meu saldo de pontos deve ser 0

  Cenário: Resgate confirmado depois de acumular pontos pedalando
    Dado que registrei uma viagem de 15 km usando "Bicicleta"
    Quando tento resgatar a recompensa "Desconto 10% em Bike Share"
    Então o resgate deve ser confirmado
    E meu saldo de pontos deve ser 50
