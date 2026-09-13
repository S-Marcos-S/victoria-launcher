### 🚀 Novidades e Melhorias

- **Novos Estilos de Relógio para a Tela Inicial e Tela de Seleção (Grade de 2 Colunas):**
  - Implementada uma nova tela dedicada de seleção de estilos de relógio (`ClockStylePickerScreen`), organizada em grade moderna de 2 colunas com cartões arredondados e pré-visualização em tempo real de cada modelo.
  - Adicionados 6 estilos de relógio elegantes e minimalistas para a tela inicial:
    - **Clássico:** Formato tradicional com hora grande e data detalhada por extenso logo abaixo.
    - **Empilhado (Stacked / Pixel style):** Horas em destaque no topo e minutos alinhados logo abaixo em duas linhas, acompanhado de data compacta.
    - **Minimalista:** Linha única limpa e discreta com tipografia fina e elegante.
    - **Analógico Moderno:** Mostrador circular com ponteiros desenhados em Canvas, marcadores de 12 horas e data.
    - **Cartão Digital:** Horário e data encapsulados em um cartão translúcido arredondado no estilo *frosted glass*.
    - **Dia em Destaque:** Dia da semana e data em destaque superior em caixa alta, com o horário grande posicionado logo abaixo.
  - O seletor pode ser acessado tanto pelo menu de **Opções da tela inicial** (ao segurar em qualquer espaço vazio) quanto pela tela de **Configurações → Aparência**.
  - Mantida a funcionalidade de toque interativo em todos os estilos: tocar nas horas abre o despertador/relógio do dispositivo e tocar na data abre o calendário.

- **Player de Música (Em Reprodução) com Barra de Progresso e Ajuste de Layout:**
  - A capa do álbum e o título da música agora acompanham exatamente as dimensões configuradas pelo usuário para os ícones e rótulos dos aplicativos (`iconSizeDp` e `labelSizeSp`), mantendo total harmonia e consistência visual na tela inicial.
  - Adicionada barra de progresso dinâmica em tempo real no estilo Material Design (Material 3 `LinearProgressIndicator` arredondado com trilha suave) exibindo o avanço da faixa musical.
  - Afastados os botões de controle de mídia em relação à barra lateral do alfabeto: o bloco agora respeita a margem de respiro de 48dp no lado do alfabeto e possui espaçamento compacto otimizado para evitar toques acidentais na rolagem A-Z.
  - Corrigido o problema em que o player de música não aparecia na tela inicial mesmo quando ativado nas configurações caso um widget estivesse configurado.
  - Adicionada pré-visualização completa do player no modo de edição de layout mesmo quando nenhuma música estiver tocando, permitindo ajustar a altura do player e os espaçamentos facilmente.
  - Aprimorada a detecção e reconexão de sessões ativas de mídia pelo serviço de notificações.

- **Animação Contínua e Ininterrupta do Alfabeto na Tela Inicial:**
  - Corrigido o desaparecimento da barra do alfabeto e interrupção da deformação elástica ao passar pelo ícone de estrela (`★`) ou estar com a tela inicial visível: a curvatura e as letras agora continuam 100% ativas e responsivas ao dedo sem sumir.
  - O encerramento do gesto agora aguarda a suavização da mola elástica antes de fixar a tela inicial, garantindo uma transição orgânica e contínua.

- **Menu de Opções do Aplicativo com Quinas Arredondadas e Blur:**
  - Modernização completa da janela de configuração e opções que se abre ao tocar e segurar em qualquer aplicativo (tanto na tela inicial / favoritos quanto na lista de todos os aplicativos).
  - Novo design translúcido em estilo *frosted glass* com quinas bem arredondadas (28dp), borda sutil iluminada e efeito de desfoque de fundo do sistema (*blur behind*).
  - Cabeçalho aprimorado com ícone do aplicativo em tamanho ampliado, nome em destaque e identificador do pacote.
  - Botões de ação arredondados com ícones elegantes, divisórias sutis e destaque em vermelho para opções de remoção/desinstalação.
  - Janelas de renomear/editar aplicativo e criar/editar pasta atualizadas com o mesmo padrão visual de quinas arredondadas e blur.

- **Janela Flutuante para Pastas de Aplicativos:**
  - Adicionada opção nas configurações ("Janela flutuante para pastas") para abrir os aplicativos contidos em uma pasta dentro de uma janela flutuante com visual translúcido, blur (*frosted glass*), grade organizada de ícones, título da pasta e atalhos rápidos.
  - Ativada por padrão para uma experiência moderna e limpa ao navegar em pastas; caso o usuário desative, as pastas voltam a se expandir em lista diretamente na tela de favoritos.

- **Formato Padrão de Versão no Menu de Opções ("Nova versão disponível"):**
  - Substituído o hash/código de commit (`7e69a5d`) no badge verde ao lado de "Nova versão disponível" pelo número padrão de versão (ex: `v0.48.4`).
  - O launcher agora extrai e formata o número de versão padrão a partir das informações de release e do aplicativo.
  - No balão de novidades ("O que há de novo"), a versão é exibida em destaque no cabeçalho acompanhada do identificador de compilação.

- **Visualização Imediata da Tela Inicial e Correção da Rolagem na Estrela (`★`):**
  - Corrigido o bug que interrompia/fechava a rolagem do alfabeto ao passar o dedo sobre o ícone da estrela (`★`): a rolagem pelo alfabeto agora permanece 100% contínua e ativa enquanto o dedo estiver na tela.
  - Ao passar pela estrela, a tela inicial é revelada imediatamente, e ao continuar deslizando para baixo para as letras do alfabeto (como 'A', 'B'), os aplicativos ressurgem suavemente sem interrupção.
  - Ao soltar o dedo na estrela, a gaveta fecha e o usuário permanece na tela inicial com seus favoritos e widgets.

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
