### 🚀 Novidades e Melhorias da Versão 0.59.8

- **Correção Completa de Widgets Fantasmas e Backup:**
  - Corrigido o bug onde o backup armazenava referências a widgets locais que, ao serem restaurados (ou após reinstalação/migração), deixavam espaços invisíveis e vazios na tela inicial.
  - IDs de widgets locais e efêmeros deixaram de ser exportados para backups, prevenindo a criação de widgets inválidos.
  - Ao restaurar qualquer backup (incluindo backups antigos), o launcher agora valida os widgets com o `AppWidgetManager` do sistema e descarta automaticamente qualquer ID inexistente ou corrompido.
- **Detecção e Limpeza Automática de Widgets Inválidos:**
  - A tela inicial agora valida todos os widgets ativos em tempo real; se um widget não existir mais no sistema, seu espaço vazio é imediatamente removido e o layout da tela inicial é normalizado sem ocupar altura fantasma.
  - Adicionada rotina de saneamento automático em segundo plano que remove referências órfãs de widgets das preferências.
- **Card Interativo e Opção de Remoção para Widgets Indisponíveis:**
  - Caso um widget venha a falhar ou ter seu app desinstalado, o launcher exibe um card visível ("Widget indisponível - Toque ou segure para remover").
  - O menu de opções por clique longo foi atualizado para sempre disponibilizar a ação de **Remover**, permitindo excluir o widget mesmo sem informações do provedor.
