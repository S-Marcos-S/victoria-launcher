### 🚀 Novidades e Melhorias da Versão 0.59.1

- **Seleção e Acesso à Pasta de Destino dos Backups (`SAF` / `Storage Access Framework`):**
  - **Sobrevivência à Desinstalação:** Agora o usuário pode escolher e conceder acesso a uma pasta externa no dispositivo (ex: *Documentos* ou *Downloads*) para armazenar seus backups. Dessa forma, mesmo que o aplicativo seja desinstalado, todos os arquivos `.victoriabackup` permanecem intactos na memória do aparelho.
  - **Liberação Condicional Segura:** A opção de criar backups (manuais ou automáticos) só é desbloqueada após a seleção e confirmação da pasta de destino, prevenindo backups órfãos ou perdas acidentais de dados.
  - **Card de Gerenciamento da Pasta:** Exibição clara no topo da tela indicando a pasta ativa vinculada com botão de alteração rápida a qualquer momento.
  - **Descoberta Automática:** Ao selecionar ou reconectar a pasta após reinstalar o app, todos os backups anteriores existentes nela são listados e disponibilizados imediatamente para restauração.

- **Correção Visual dos Botões de Alternância (`Switch`):**
  - **Visibilidade Perfeita da Bolinha Ativada:** Ajustadas as cores do botão de alternância quando ativado. O marcador interno (*bolinha* / *thumb*) agora utiliza a cor de alto contraste (`onPrimary`) sobre o fundo colorido (`primary`), corrigindo o problema visual em que a bolinha desaparecia por ter a mesma cor do fundo.
