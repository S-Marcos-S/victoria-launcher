### 🚀 Novidades e Melhorias da Versão 0.59.0

- **Sistema Completo de Backup e Restauração (`BackupManager` / `BackupSettingsScreen`):**
  - **Backup Completo de Preferências e Layout:** Agora você pode exportar todas as suas preferências do Victoria Launcher em um único pacote `.victoriabackup` seguro e compacto. Inclui favoritos, pastas, estilos de relógio, fontes, tamanhos, ícones personalizados e ajustes de tela inicial.
  - **Inclusão do Papel de Parede:** Opção integrada (habilitada por padrão) que extrai e arquiva o papel de parede do sistema no arquivo de backup, restaurando-o automaticamente ao aplicar o backup.
  - **Backups Automáticos em Segundo Plano:** Agendamento periódico de backups automáticos com frequência personalizável (Diária, Semanal ou Mensal), persistido mesmo após reiniciar o dispositivo (`RECEIVE_BOOT_COMPLETED`).
  - **Controle de Retenção e Espaço:** Escolha quantos arquivos de backup manter salvos (3, 5, 10 ou ilimitado). Arquivos mais antigos são podados de forma inteligente para economizar espaço de armazenamento.
  - **Compartilhamento e Restauração de Arquivos Externos:** Compartilhe seus backups com outros dispositivos ou serviços de nuvem via `FileProvider`, e restaure qualquer backup externo através do seletor de arquivos do sistema (`SAF`).
  - **Interface Dedicada nas Configurações:** Nova seção e tela de gerenciamento de backups com visual Material 3 fluido, listando backups salvos, etiquetas de tipo (Manual / Automático), indicação de papel de parede e diálogos de confirmação para restauração e exclusão seguras.
