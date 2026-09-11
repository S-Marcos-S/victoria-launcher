### 🚀 Novidades e Melhorias da Versão

- **Menu da Tela Inicial (Long-Press):**
  - Adicionada animação fluida de abertura (slide up com spring e fade-in) e de fechamento (slide down suave com fade-out).
  - Adicionado suporte ao gesto de arrastar o painel para baixo para fechar (swipe down to dismiss) com retorno elástico.
  - Suporte ao botão/gesto de voltar do Android (`BackHandler`).
  - Novo card de **Atualização Disponível** exibido apenas quando há uma nova build release no GitHub.
  - Dois botões lado a lado: **"Mudanças"** (abre este balão flutuante com transparência e blur) e **"Atualizar"** (baixa o APK release diretamente para o dispositivo).

- **Balão Flutuante de Mudanças (Changelog):**
  - Janela flutuante com efeito frosted-glass (transparência elegante) e desfoque nativo do sistema (`FLAG_BLUR_BEHIND`) no fundo da tela.
  - Exibe hash do commit, data/hora da compilação, tamanho do arquivo e lista detalhada de implementações.

- **Toque Duplo para Desligar a Tela:**
  - Habilitado na tela inicial e na lista de aplicativos.
  - Animação de colapso visual escurecendo a tela de fora para dentro exatamente no ponto do toque antes de bloquear o aparelho.
  - Verificação do serviço de acessibilidade com botão de atalho direto nas configurações.

- **Automação e Distribuição:**
  - Integração do changelog automático nas releases do GitHub Actions.
  - Script para acompanhar a compilação em tempo real e salvar o APK release diretamente na pasta Downloads.
