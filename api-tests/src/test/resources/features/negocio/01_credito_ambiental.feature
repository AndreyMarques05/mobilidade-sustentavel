# language: pt
@negocio @esg-ambiental
Funcionalidade: Crédito ambiental por viagens sustentáveis
  Como usuário que troca o carro por um modal sustentável
  Quero que cada viagem registre o CO2 que deixei de emitir
  Para acompanhar meu impacto ambiental e acumular pontos

  O CO2 economizado é a diferença entre a emissão de um carro a combustão
  (0,21 kg por km, referência CETESB) e a emissão do modal escolhido,
  multiplicada pela distância. Os pontos são a distância vezes os pontos
  por km do modal.

  Contexto:
    Dado que sou um usuário recém-cadastrado, ainda sem pontos

  Cenário: Viagem de bicicleta gera crédito de CO2 e pontos
    Quando registro uma viagem de 12,5 km usando "Bicicleta"
    Então a viagem deve registrar 2,625 kg de CO2 economizado
    E devo ganhar 125 pontos
    E meu saldo de pontos deve ser 125

  Esquema do Cenário: O crédito depende da emissão de cada modal
    Quando registro uma viagem de 10 km usando "<modal>"
    Então a viagem deve registrar <co2> kg de CO2 economizado
    E devo ganhar <pontos> pontos

    Exemplos:
      | modal               | co2  | pontos |
      | Bicicleta           | 2,10 | 100    |
      | Caminhada           | 2,10 | 80     |
      | Metrô / Trem        | 2,05 | 50     |
      | Patinete Elétrico   | 1,94 | 70     |
      | Ônibus Convencional | 1,45 | 30     |
      | Carro Híbrido       | 0,90 | 20     |

  Cenário: O painel de impacto soma todas as viagens do usuário
    Quando registro uma viagem de 5 km usando "Bicicleta"
    E registro uma viagem de 10 km usando "Ônibus Convencional"
    Então meu painel de impacto deve mostrar 2 viagens e 2,5 kg de CO2 economizado
    E meu saldo de pontos deve ser 80

  Cenário: Corrigir o modal de uma viagem refaz o crédito e o saldo
    Dado que registrei uma viagem de 10 km usando "Bicicleta"
    Quando corrijo a viagem para 10 km usando "Carro Híbrido"
    Então a viagem corrigida deve registrar 0,90 kg de CO2 economizado
    E devo ganhar 20 pontos
    E meu saldo de pontos deve ser 20
