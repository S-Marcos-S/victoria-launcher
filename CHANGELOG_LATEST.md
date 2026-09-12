### 🚀 Novidades e Melhorias

- **Atualização da Documentação (README.md):**
  - Removidas referências legadas exclusivas do repositório upstream (como selo do F-Droid e notas de assinatura de chave antigas).
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
