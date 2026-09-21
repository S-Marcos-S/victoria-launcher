### 🚀 Novidades e Melhorias da Versão 0.59.12

- **Restauração do Timing Original de Desligamento de Tela (`ScreenOffEffect`):**
  - Revertida a antecipação de disparo do comando de bloqueio de tela para o momento de conclusão total da animação de colapso circular (`progress.animateTo` a 100%).
  - Mantida a estabilidade da sobreposição preta até o despertar do dispositivo (`ON_START` / `ON_RESUME`), prevenindo a reexibição prematura da tela inicial.
