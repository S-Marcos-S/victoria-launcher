### 🚀 Novidades e Melhorias da Versão 0.59.10

- **Correção da Piscada (Flash) ao Desligar a Tela com Toque Duplo (`ScreenOffEffect`):**
  - Corrigido o problema onde a tela inicial reaparecia brevemente (piscava) logo após a conclusão da animação de colapso circular antes de desligar completamente o visor.
  - O estado da animação de desligamento (`lockTargetOffset`) agora permanece ativo e sólido em preto durante toda a transição de desligamento e bloqueio (`ON_PAUSE` e `ON_STOP`), evitando que a camada preta seja descartada enquanto o painel do dispositivo ainda está finalizando o desligamento.
  - A remoção da sobreposição preta e o restabelecimento da tela inicial agora ocorrem exclusivamente quando o dispositivo é reativado/desbloqueado pelo usuário (`ON_START` / `ON_RESUME`).
  - Adicionado timeout de contingência para garantir que, caso o serviço de bloqueio do sistema operacional não processe o desligamento em tempo hábil, a interface retorne ao estado normal com segurança.
  - A animação do efeito de desligamento (`ScreenOffEffect`) foi isolada com chave estável para evitar reinicializações espúrias durante recomposições de ciclo de vida.
