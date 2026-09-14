### 🚀 Novidades e Melhorias da Versão 0.49.0

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

- **Tela de Configurações Completa com Demonstração Interativa:**
  - Nova tela dedicada de configurações acessível em Configurações → Botão Dinâmico ou segurando o próprio botão na tela inicial.
  - **Prévia Interativa ao vivo:** Permite testar os gestos e sentir as animações de borracha em tempo real diretamente na tela de configurações.
  - Seletor moderno de aplicativos com pesquisa rápida por nome e opção para limpar ou alterar a ação de cada gesto.
  - Chave geral para ativar ou desativar o botão a qualquer momento.

- **Criação Automática de Releases e Detecção Inteligente de Atualizações:**
  - **Releases Versionadas Automáticas:** O GitHub Actions agora detecta se a versão no `build.gradle.kts` mudou e, caso a tag ainda não exista no repositório, cria automaticamente uma nova release oficial (ex: `Victoria Launcher v0.49.0` com a tag `v0.49.0`), anexando os APKs e o changelog.
  - **Detecção Semântica & Seleção do APK Mais Recente (`UpdateManager`):** O aplicativo agora compara a versão semântica remota e os timestamps de assets mais recentes, identificando imediatamente novas releases ou compilações intermediárias.
  - **Aviso Automático na Tela Inicial ("O que há de novo"):** O balão flutuante translúcido com blur agora aparece automaticamente na tela inicial assim que uma nova versão é detectada, permitindo baixar diretamente ou dispensar ("Lembrar depois"), mantendo persistência de dispensa para evitar interrupções repetitivas.
  - **Máxima Eficiência de Bateria:** Checagem de atualizações otimizada com cache inteligente de 20 minutos, sem processos ou serviços em segundo plano drenando bateria.
  - **Estabilidade & Compilação:** Correção de inferência de tipos e smart-casting no gerenciador de atualizações (`UpdateManager`) e na tela inicial (`HomeRoute`).


