### 🚀 Novidades e Melhorias da Versão 0.48.3

- **Sistema de Atualizações:**
  - Lançamento da **versão 0.48.3** para validação do fluxo completo de atualização.
  - Verificação de updates em tempo real sempre que o menu da tela inicial é aberto.
  - Comparação aprimorada de integridade de versão via commit SHA contra a API do GitHub.
  - Limpeza automática de versões anteriores baixadas na pasta Downloads antes de salvar o novo APK, evitando arquivos duplicados.

- **Menu da Tela Inicial (Long-Press):**
  - Animação de abertura (slide up com spring e fade-in) e fechamento (slide down suave com fade-out).
  - Gesto de deslizar para baixo (swipe down) para fechar o menu com amortecimento elástico.
  - Suporte total ao botão/gesto voltar do sistema (`BackHandler`).
  - Card de **Atualização Disponível** com botões lado a lado:
    - **"Mudanças"**: Abre o balão flutuante translúcido com blur.
    - **"Atualizar"**: Faz o download direto do APK release para o aparelho.

- **Balão Flutuante de Mudanças (Changelog):**
  - Janela flutuante no estilo *frosted glass* translúcido com desfoque de sistema no plano de fundo (`FLAG_BLUR_BEHIND`).
  - Detalhes da release: identificador do commit, data e horário da build, tamanho do pacote e lista das novidades.

- **Toque Duplo para Desligar a Tela:**
  - Gesto disponível na tela inicial e gaveta de apps.
  - Efeito visual de foco e colapso escurecendo a tela suavemente a partir do ponto de toque.
