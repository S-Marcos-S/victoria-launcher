### 🚀 Novidades e Melhorias da Versão 0.59.15

- **Eliminação de Lags e Engasgos na Rolagem do Alfabeto:**
  - **Isolamento de Recomposição:** Encapsuladas as checagens de visibilidade da tela inicial (`showHome`) e bloqueio de toques (`blockHomeTouches`) com `derivedStateOf` em `HomeRoute`, impedindo que a mudança de letras durante o scrub recomponha a tela inicial inteira e seus widgets.
  - **Otimização do Container da Gaveta de Apps:** Ajustada a medição e posicionamento da gaveta para manter dimensões consistentes e alternar a transparência na fase de desenho (`graphicsLayer`), eliminando saltos de layout `0x0` na abertura rápida.
  - **Remoção de Nós Mortos nas Linhas da Lista (`AppRow`):** Eliminado o `DropdownMenu` obsoleto e seus 8 parâmetros não utilizados de cada linha da lista de aplicativos, reduzindo centenas de alocações inúteis de nós de composição, ícones e strings por segundo durante rolagens rápidas.
  - **Ativação de `largeHeap`:** Habilitado `android:largeHeap="true"` no manifesto para evitar pressão de coleta de lixo (GC) e descarte do cache de ícones (`IconCache`) após o celular permanecer ocioso na tela inicial.
