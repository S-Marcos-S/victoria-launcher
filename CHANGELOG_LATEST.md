### 🚀 Novidades e Melhorias da Versão 0.59.13

- **Desfoque Nativo e Estável de Papel de Parede (`WallpaperBlurManager`):**
  - Corrigida a instabilidade em que o desfoque de fundo da tela de aplicativos não aparecia de imediato ou só ativava ao iniciar o gesto de aplicativos recentes.
  - Substituída a dependência da flag instável de janela `FLAG_BLUR_BEHIND` na `MainActivity` pela renderização nativa direta do papel de parede desfocado via `WallpaperBlurManager`.
  - Implementado algoritmo otimizado de Stack Blur (`FastBlur`) em segundo plano, mantendo um buffer leve e de altíssima performance para visualização em tempo real sem engasgos na rolagem do alfabeto.
  - O desfoque na lista de apps e na tela de pesquisa agora é exibido instantaneamente no primeiro quadro, operando com total estabilidade e independente de transições ou gestos de navegação do sistema.
  - Mantida camada translúcida de vidro fosco (frosted glass) calibrada para temas claro e escuro, garantindo contraste perfeito para textos e ícones.
