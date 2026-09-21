### 🚀 Novidades e Melhorias da Versão 0.59.9

- **Opção de Desfoque em Vidro Fosco (Blur) na Lista de Aplicativos (`AppListScreen`):**
  - Adicionada opção nas configurações (*Aparência → Desfocar papel de parede na lista de apps*) para ativar o desfoque de fundo em tempo real na tela de aplicativos.
  - Implementado com a mesma tecnologia de desfoque nativo do sistema presente na tela de pesquisa (`FLAG_BLUR_BEHIND` e `blurBehindRadius = 45` no Android 12+).
  - Superfície com camada translúcida de vidro fosco calibrada para temas claro e escuro (`52%` preto em modo escuro e `58%` branco em modo claro), permitindo que as cores do wallpaper brilhem suavemente ao fundo.
  - Ajuste dinâmico das cores de tipografia, cabeçalhos de seções e atalho de configurações para alto contraste e legibilidade impecável sobre o vidro fosco.
  - Efeito de desvanecimento suave do topo da lista adaptado ao tema, evitando bordas escuras indesejadas no modo claro.
  - O índice alfabético lateral (`EdgeScrubber`) agora respeita a cor de contraste dinâmico tanto na tela inicial quanto na lista de aplicativos.
  - Transição segura e automática do desfoque de janela: o efeito é ativado dinamicamente ao abrir a gaveta de apps e imediatamente desativado ao fechar, retornar para a tela inicial ou alternar para a pesquisa.
