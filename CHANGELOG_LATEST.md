### 🚀 Novidades e Melhorias da Versão 0.59.11

- **Sincronização do Desligamento da Tela contra Flash da Tela de Bloqueio (`ScreenOffEffect`):**
  - Ajustado o ponto de disparo do comando de bloqueio e desligamento de tela (`GLOBAL_ACTION_LOCK_SCREEN`) para iniciar de forma sincronizada na reta final da animação de colapso circular (`progresso >= 85%`).
  - Ao antecipar o comando nos últimos ~60ms enquanto o círculo fecha até o centro, o `PowerManager` do Android e o hardware do display iniciam o corte de energia do visor simultaneamente ao término da animação.
  - Isso elimina a condição de corrida de milissegundos no sistema operacional onde a tela de bloqueio nativa (`Keyguard`) conseguia renderizar um frame no topo do overlay antes do painel físico apagar.
  - Transição de desligamento suave, estável e completamente escura, sem flashes da tela de bloqueio.
