### 🚀 Novidades e Melhorias da Versão 0.50.0

- **Botão de Alinhamento Rápido na Tela de Edição do Layout:**
  - **Restauração de Alinhamento com 1 Toque:** Adicionado o botão *"Alinhar elementos"* na barra de ações superior do modo de edição/ordenação da tela inicial (*Editar layout*).
  - **Alinhamento com Base nos Aplicativos e na Borda:** Caso o usuário tenha desalinhado os componentes ao arrastar o puxador de espaçamento lateral ou os puxadores verticais, o botão restaura instantaneamente a distância padrão e simétrica dos aplicativos em relação à borda do celular (`sidePaddingDp = 20dp`), alinhando de imediato o alfabeto, os aplicativos, os widgets, o relógio e o reprodutor de mídia.
  - **Redefinição Harmônica dos Espaçamentos:** O botão redefine com precisão todos os espaçamentos padrão entre os blocos (`HomePaddings.Default`), garantindo simetria perfeita sem folgas excessivas.
  - **Feedback Háptico e Visual:** Acionamento com vibração háptica agradável e confirmação instantânea em tela (*"Todos os elementos foram alinhados com base nos aplicativos"*).

- **Substituição do Pop-up Automático de Atualização por Notificação do Sistema:**
  - **Experiência Não Intrusiva na Tela Inicial:** O diálogo/balão flutuante de mudanças ("O que há de novo") não surge mais de forma involuntária ou automática cobrindo a tela inicial ao detectar novas versões do aplicativo.
  - **Notificação Discreta e Informativa do Sistema:** Ao identificar que uma nova versão foi compilada ou lançada, o launcher agora emite uma notificação elegante na barra de status do Android informando a versão disponível (*"Victoria Launcher vX.Y.Z disponível"* - *"Toque para ver o que há de novo e atualizar"*).
  - **Acesso Direto sob Demanda:** Ao tocar na notificação, o aplicativo é trazido ao primeiro plano e o diálogo com o que há de novo e botão de download/instalação é exibido para o usuário.
  - **Integração Total com o Painel de Opções:** A verificação manual de atualizações e visualização das novidades permanecem sempre disponíveis no painel de opções da tela inicial (`HomeOptionsBottomSheet`).
  - **Compatibilidade com Android 13+:** Implementado canal de notificação dedicado (`victoria_launcher_updates`), tratamento seguro de permissões em tempo de execução (`POST_NOTIFICATIONS`) e controle inteligente de cache para evitar notificações repetidas da mesma versão.

- **Alinhamento Automático e Harmônico (Alfabeto, Aplicativos, Widgets e Relógio):**
  - **Grid de Alinhamento Unificado e Automático:** Implementado um sistema de alinhamento harmônico entre a borda do alfabeto (`EdgeScrubber`), a lista de aplicativos favoritos e pastas (`FavoriteRow` e `FolderRow`), os widgets (`WidgetSlot` e `NowPlayingBlock`) e o relógio da tela inicial (`NiagaraClockWidget`).
  - **Padrão Harmônico por Padrão:** Todos os componentes alinham-se automaticamente às mesmas diretrizes verticais:
    - *Margem externa da tela:* Relógio, widgets e ícones de aplicativos iniciam no espaçamento padrão do sistema (`sidePaddingDp`), e a coluna do alfabeto espelha exatamente a mesma distância na borda oposta da tela.
    - *Margem interna do alfabeto:* O relógio, os widgets, o reprodutor de mídia e as linhas de aplicativos respeitam automaticamente um espaçamento calculado com precisão (`sidePaddingDp + 32dp`), impedindo qualquer sobreposição com o alfabeto ou sua zona de toque.
  - **Sincronização Dinâmica com o Puxador de Ajuste (`SidePaddingHandle`):** Caso o usuário decida personalizar o recuo das bordas nas configurações ou no modo de edição arrastando a barra de ajuste lateral, **todos** os quatro elementos (alfabeto, aplicativos, widgets e relógio) adaptam-se em conjunto de forma automática e em tempo real.
  - **Suporte Abrangente ao Modo Destro (`alignRight`) e Posição do Alfabeto (`edgeSide`):** Seja com o layout padrão à esquerda, modo destro à direita, alfabeto à esquerda ou em ambos os lados, as margens `contentStart` e `contentEnd` ajustam-se dinamicamente com perfeita harmonia visual.

- **Cores Dinâmicas do Sistema (Material You / Monet) em Todas as Janelas e Diálogos:**
  - **Color Tinting Dinâmico de Superfície (`dynamicSurfaceColor`):** As superfícies das janelas e diálogos não ficam mais em cinza neutro estático. Implementado o algoritmo de mesclagem (`lerp`) que infunde a cor primária dinâmica do wallpaper (Monet) no container de vidro fosco translúcido (`FLAG_BLUR_BEHIND`), garantindo que o fundo translúcido assuma a tonalidade real do papel de parede (azul, verde, roxo, âmbar, etc.).
  - **Bordas Dinâmicas com Brilho do Wallpaper (`dynamicBorderColor`):** Contorno sutil com tonalidade adaptativa derivada da cor primária Monet, delimitando com elegância e sofisticação todas as janelas flutuantes.
  - **Pills Circulares e Ícones Tematizados no Painel de Opções (`HomeOptionsBottomSheet`):**
    - Todas as opções (Configurações, Editar tela inicial, Papel de parede, Adicionar widget, Botão dinâmico) agora contam com ícones aninhados em elegantes círculos com fundo dinâmico suave (`primary.copy(alpha = 0.14f)`) e ícones em destaque na cor primária Monet.
    - Puxador superior (drag handle) adaptado na cor dinâmica primária do sistema.
    - Fundo do painel inferior suavemente tingido na tonalidade do wallpaper com borda superior harmônica.
  - **Menu de Contexto de Aplicativos (`AppMenuDialog`):**
    - Ações aninhadas em círculos dinâmicos temáticos (com destaque em vermelho semântico de erro para desinstalação e primário para as demais ações).
    - Subtítulo do nome de pacote na cor primária e divisor estilizado com transparência dinâmica.
  - **Janela Flutuante de Pastas (`FolderFloatingDialog`):**
    - Badge de pasta em `primaryContainer`, botões de ação circulares (Configurações e Fechar) com fundo e ícones na cor primária, e subtítulo de contagem em destaque dinâmico.
  - **Diálogos de Edição (`EditAppDialog` e `FolderEditDialog`):**
    - Badge de cabeçalho com ícone de edição estilizado, botão de alterar ícone com borda dinâmica e botão de confirmação destacado em botão preenchido com a cor primária (`primary`).
  - **Diálogos de Notificações (`NotificationDetailDialog`) e Seleção de Pasta (`FolderPickerDialog`):**
    - Botões de ação em cores primárias dinâmicas, fundos adaptados ao papel de parede e divisores harmônicos.
  - **Suporte Abrangente a Versões do Android (8.1+ a 15):**
    - Suporte nativo ao Monet no Android 12+ via `dynamicDarkColorScheme`/`dynamicLightColorScheme`.
    - Extração automática de cores primárias do wallpaper via `WallpaperManager` no Android 8.1 a 11, garantindo que mesmo versões anteriores do sistema desfrutem de janelas tingidas na cor do papel de parede.
  - **Estabilidade & Compilação:** Correção de imports de dimensionamento de layout (`size`/`width`) no diálogo de edição de aplicativos (`EditAppDialog`).


- **Novo Botão Dinâmico na Tela Inicial:**
  - Implementado um botão de atalhos dinâmico posicionado estrategicamente na parte inferior da tela, ao lado da coluna do alfabeto, facilitando o alcance ergonômico com uma mão.
  - O botão adapta-se automaticamente à mão preferida do usuário (lado esquerdo ou direito), posicionando-se ao lado do alfabeto sem sobrepor as letras e sem interferir na zona de deslizamento da lista A-Z.
  - **Posicionamento Elevado Otimizado:** O botão foi elevado para ficar com espaçamento confortável acima da barra de navegação do sistema, permitindo realizar o gesto de puxar para baixo com total amplitude sem interferir nos gestos de navegação do sistema.
  - **Ícone Tematizado com Cor Dinâmica do Sistema:** O botão dinâmico exibe o ícone do aplicativo definido para o clique renderizado com a cor dinâmica do sistema (Material You dynamic theming), aproveitando a camada monocromática oficial do app (Android 13+) ou processamento inteligente de contraste, mantendo perfeita coerência visual com o papel de parede.
  - Totalmente estável e visível com exclusividade na tela inicial (ocultando-se suavemente ao abrir a lista de aplicativos ou entrar em modo de edição).

- **Três Gestos Personalizáveis:**
  - **Toque rápido (Clique):** Abre o aplicativo principal configurado com micro-animação elástica de compressão e descompressão rápida com overshoot.
  - **Puxar para cima (Swipe Up):** Permite abrir um segundo aplicativo através do gesto de puxar para cima.
  - **Puxar para baixo (Swipe Down):** Permite abrir um terceiro aplicativo através do gesto de puxar para baixo.
  - **Toque longo (Pressionar e segurar):** Abre instantaneamente a tela de personalização do botão dinâmico para trocar ou redefinir os aplicativos atribuídos.

- **Animações Profissionais Estilo Borracha & Transições Direcionais de Ícone:**
  - **Estabilidade de Posição sem Deslocamento Lateral:** O botão dinâmico agora possui dimensões fixas no container raiz e medição desvinculada para os balões, eliminando qualquer salto ou deslocamento horizontal involuntário no momento em que o dedo inicia o arrasto.
  - **Balão Indicador Focado e Minimalista:** Durante o gesto de puxar para cima ou para baixo, o balão flutuante exibe exclusivamente o nome do aplicativo de destino em um elegante pill translúcido, mantendo a tela limpa.
  - **Transição de Ícone Estilo Carrossel/Roleta Interna:** Ao puxar o botão, o ícone do aplicativo de destino surge de dentro do botão substituindo o ícone de toque:
    - *Ao puxar para cima:* O ícone atual desliza para baixo sumindo da vista, enquanto o ícone do app de cima entra rolando de cima para baixo com mola.
    - *Ao puxar para baixo:* O ícone atual desliza para cima sumindo da vista, enquanto o ícone do app de baixo entra rolando de baixo para cima.
    - *Ao soltar ou cancelar:* O ícone de toque retorna suavemente na direção oposta ao ponto de repouso.
  - Todos os ícones exibidos mantêm fidelidade total ao tema dinâmico do sistema (Material You dynamic theming).
  - Física elástica real com resistência não linear hiperbólica (`tanh`), deformação Squash & Stretch de volume conservado e retorno elástico com `Spring.DampingRatioMediumBouncy`.

- **Tela de Configurações Simplificada e Focada:**
  - Nova tela dedicada acessível em Configurações → Botão Dinâmico ou segurando o próprio botão na tela inicial.
  - Layout direto e limpo com seletor moderno de aplicativos, pesquisa rápida e opção para redefinir as ações de cada gesto.
  - Chave geral para ativar ou desativar o botão a qualquer momento.

- **Criação Automática de Releases e Detecção Inteligente de Atualizações:**
  - **Releases Versionadas Automáticas:** O GitHub Actions agora detecta se a versão no `build.gradle.kts` mudou e, caso a tag ainda não exista no repositório, cria automaticamente uma nova release oficial (ex: `Victoria Launcher v0.49.0` com a tag `v0.49.0`), anexando os APKs e o changelog.
  - **Detecção Semântica & Seleção do APK Mais Recente (`UpdateManager`):** O aplicativo agora compara a versão semântica remota e os timestamps de assets mais recentes, identificando imediatamente novas releases ou compilações intermediárias.
  - **Aviso Automático na Tela Inicial ("O que há de novo"):** O balão flutuante translúcido com blur agora aparece automaticamente na tela inicial assim que uma nova versão é detectada, permitindo baixar diretamente ou dispensar ("Lembrar depois"), mantendo persistência de dispensa para evitar interrupções repetitivas.
  - **Máxima Eficiência de Bateria:** Checagem de atualizações otimizada com cache inteligente de 20 minutos, sem processos ou serviços em segundo plano drenando bateria.
- **Aprimoramento do Ícone de Estrela e Correção da Animação do Alfabeto:**
  - **Correção da Visibilidade do Alfabeto na Estrela:** Corrigida a falha onde a animação e a visualização do alfabeto (`EdgeScrubber`) desapareciam ao passar o dedo sobre a estrela ou ao iniciar o gesto de rolagem a partir da tela inicial. O alfabeto e a bolha de prévia foram desacoplados da camada de esmaecimento da lista, mantendo a animação elástica e o feedback visual 100% visíveis e estáveis.
  - **Transição Suave de Retorno:** A alternância entre a lista de aplicativos e a tela inicial ao atingir a estrela agora conta com cross-fade animado suave (`starAlpha`), sem cortes bruscos ou perda do gesto.
  - **Eliminação do Flash da Letra 'A' ao Soltar a Estrela:** Corrigida a aparição momentânea da lista de aplicativos da letra "A" ao soltar o dedo sobre o ícone de estrela. A dispensa da lista agora é executada de forma imediata e síncrona no encerramento do gesto, cancelando o estado do gesto sem atrasos de mola e garantindo que a tela inicial permaneça visível sem qualquer piscamento ou transição indesejada.
  - **Ícone de Estrela Refinado:** Atualizado para `Icons.Rounded.Star` com pontas arredondadas e proporção óptica equilibrada (13dp na coluna do alfabeto e 38dp na bolha flutuante), integrando-se perfeitamente à tipografia e ao visual moderno do sistema.
  - **Estabilidade & Compilação:** Correção de inferência de tipos e smart-casting no gerenciador de atualizações (`UpdateManager`) e na tela inicial (`HomeRoute`).



