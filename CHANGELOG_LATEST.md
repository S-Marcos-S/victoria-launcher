### 🚀 Novidades e Melhorias da Versão 0.58.1

- **Correção e Redesign da Tela de Editar Layout (`HomeScreen`):**
  - **Respeito aos Insets da Barra de Status:** Adicionado recuo superior automático baseado em `WindowInsets.statusBars`, evitando que os botões e textos do modo de edição fiquem sob a barra de notificações, notch ou recorte da câmera.
  - **Novo Card Superior com Layout em Duas Linhas:** Reestruturada a barra superior de edição para evitar que os textos e botões fiquem espremidos ou cortados. A primeira linha agora abriga o título e os botões de ação (*"Alinhar"* e *"Concluído"*), enquanto a segunda linha apresenta a dica de reordenação com largura total e sem truncamentos.
  - **Eliminação do Espaçamento Rígido Lateral:** Removido o padding desnecessário de 72dp no canto direito que espremia as informações no topo.
  - **Alças de Ajuste e Espaçamento com Alto Contraste:** As alças de arrasto de padding lateral, vertical e de altura de widgets agora utilizam o tema do sistema (`surfaceContainer`, `onSurface` e `dynamicBorderColor`), garantindo contraste nítido e legibilidade tanto em papéis de parede claros quanto escuros.
  - **Espaçamento Inferior Seguro:** Adicionada folga na base da lista para que as últimas alças não sejam encobertas pela barra de navegação ou pílula de gestos do Android.

- **Solicitação de Inicializador Padrão na Primeira Abertura (`SetDefaultLauncherDialog`):**
  - **Detecção Automática de Primeiro Acesso:** Ao abrir o Victoria Launcher pela primeira vez, a aplicação detecta se já é o inicializador padrão do sistema antes de exibir qualquer diálogo.
  - **Design Translúcido em Vidro Fosco (Frosted Glass & Blur):** Diálogo flutuante moderno com suporte a desfoque de fundo nativo (`FLAG_BLUR_BEHIND` e `blurBehindRadius = 32`), cantos arredondados de 24dp e contorno suave tingido pelo tema dinâmico Material You.
  - **Destaque Visual das Vantagens:** Apresenta de forma elegante os diferenciais do Victoria Launcher (retorno instantâneo ao pressionar Home, rolagem rápida no alfabeto A-Z e interface limpa sem distrações).
  - **Integração com Android Q+ (RoleManager) e Legados:** Acionamento do seletor nativo do sistema via `RoleManager.ROLE_HOME` em aparelhos com Android 10 ou superior, com fallback seguro para configurações de apps padrão em versões anteriores.
  - **Ações Intuitivas:** Botão de confirmação para abrir o seletor com um toque e botão *"Agora não"* para dispensar sem insistências futuras.

- **Opção de Definir Inicializador Padrão nas Configurações (`SettingsScreen`):**
  - Nova opção na seção *Comportamento* das Configurações do Victoria Launcher.
  - Exibe o status em tempo real (se o Victoria Launcher já é o padrão ou não) e permite alterar ou definir como padrão a qualquer momento.

- **Otimização do Histórico de Versões e Releases do GitHub:**
  - **Changelog Enxuto:** `CHANGELOG_LATEST.md` agora contém estritamente as alterações da versão mais recente, eliminando o acúmulo de versões passadas nas notas de release do GitHub e no diálogo de atualização do launcher.
  - **Arquivo Histórico Completo:** Todas as notas e registros das versões anteriores foram preservados no novo arquivo `CHANGELOG.md`.
  - **Automação no GitHub Actions (`build.yml`):** O workflow agora extrai com precisão cirúrgica apenas o resumo da versão mais recente para o corpo das releases do GitHub.
