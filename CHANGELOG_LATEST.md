### 🚀 Novidades e Melhorias

- **Janela Flutuante para Pastas de Aplicativos:**
  - Adicionada opção nas configurações ("Janela flutuante para pastas") para abrir os aplicativos contidos em uma pasta dentro de uma janela flutuante com visual translúcido, blur (*frosted glass*), grade organizada de ícones, título da pasta e atalhos rápidos.
  - Ativada por padrão para uma experiência moderna e limpa ao navegar em pastas; caso o usuário desative, as pastas voltam a se expandir em lista diretamente na tela de favoritos.

- **Formato Padrão de Versão no Menu de Opções ("Nova versão disponível"):**
  - Substituído o hash/código de commit (`7e69a5d`) no badge verde ao lado de "Nova versão disponível" pelo número padrão de versão (ex: `v0.48.4`).
  - O launcher agora extrai e formata o número de versão padrão a partir das informações de release e do aplicativo.
  - No balão de novidades ("O que há de novo"), a versão é exibida em destaque no cabeçalho acompanhada do identificador de compilação.

- **Visualização Imediata da Tela Inicial ao Rolar até a Estrela (`★`):**
  - Ao rolar o alfabeto lateral até a estrela, a gaveta de aplicativos fica invisível e a tela inicial (com os aplicativos favoritos e widgets) é exibida em primeiro plano imediatamente durante a rolagem.
  - Ao continuar rolando para baixo para qualquer letra (ex: 'A'), a lista de aplicativos ressurge suavemente.
  - Ao soltar o dedo na estrela, a gaveta é fechada e o usuário permanece na tela inicial.

- **Correção de Duplicação de Contatos/Conversas nas Notificações (ex: WhatsApp):**
  - Corrigida a duplicação do nome da pessoa / card no topo do balão de notificação quando múltiplos alertas chegam para a mesma conversa.
  - O serviço de notificações agora filtra resumos de grupo (`FLAG_GROUP_SUMMARY`) redundantes quando notificações individuais da conversa já estão ativas.
  - O balão de detalhes de notificação agora agrupa e deduplica automaticamente conversas com o mesmo título, exibindo os chips de alternância apenas quando existirem múltiplos contatos distintos.

- **Atualização da Documentação (README.md):**
  - Atualizadas as instruções de download e releases apontando diretamente para as versões deste repositório, com descrição completa das novas funcionalidades.

- **Animação Fluida e Contínua de Rolagem no Alfabeto:**
  - Corrigida a interrupção da animação de rolagem ao passar pelo ícone de estrela (`★`): o gesto agora preserva a deformação elástica e a curva gaussiana de forma 100% contínua e suave, mesmo ao rolar sobre a estrela e continuar rolando para baixo.
  - O retorno para a tela inicial com os favoritos agora ocorre no momento da liberação do toque (ao soltar o dedo ou dar um toque sobre a estrela), eliminando qualquer travamento ou perda de animação durante o arraste.

- **Correção do Botão "Abrir" no Balão de Notificações:**
  - Corrigido o botão de abrir o aplicativo que não disparava a ação: implementado envio de `PendingIntent` com `ActivityOptions` (`MODE_BACKGROUND_ACTIVITY_START_ALLOWED` no Android 14+) e flag `FLAG_ACTIVITY_NEW_TASK`.
  - Adicionado mecanismo de fallback automático que abre o aplicativo diretamente caso a intenção da notificação falhe ou não esteja disponível.
  - Fechamento imediato da gaveta de aplicativos ao abrir a notificação, levando o usuário diretamente ao app aberto.

- **Exibição de Notificações e Mensagens em Lista:**
  - Suporte completo a conversas com múltiplas mensagens (`Notification.MessagingStyle`, `InboxStyle` e mensagens históricas).
  - Ao expandir a notificação no balão translúcido com blur, todas as mensagens enviadas pela pessoa aparecem estruturadas em lista, com identificação de remetente, texto completo e horário de cada mensagem.
  - Adicionado suporte a múltiplas conversas do mesmo aplicativo com abas/chips no topo do balão para alternar facilmente entre elas.
  - Indicador de quantidade de mensagens na linha de notificação abaixo do nome do app (ex: `(4)`).

- **Ícone de Estrela para Favoritos no Alfabeto:**
  - Adicionado ícone de estrela (`★`) no topo da barra alfabética lateral, posicionado diretamente acima da letra 'A'.
  - A estrela é referente aos Favoritos e Widgets da tela inicial.
  - Ao rolar o alfabeto até a estrela e soltar (ou tocá-la) na lista de aplicativos, o launcher sobe diretamente para a tela inicial onde aparecem os favoritos e widgets.
  - Indicador visual com ícone de estrela estilizado dentro do balão flutuante durante a rolagem rápida pela lateral.
