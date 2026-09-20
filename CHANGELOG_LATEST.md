### 🚀 Novidades e Melhorias da Versão 0.59.7

- **Abertura Confiável das Configurações de Launcher Padrão:**
  - Corrigido o problema onde o botão "Definir como padrão" no diálogo da tela inicial e nas configurações não abria a tela de configurações do sistema.
  - Implementada priorização direta do `Settings.ACTION_HOME_SETTINGS` com flags adequadas, garantindo que o sistema abra instantaneamente a tela de seleção de "App de início padrão" em todas as versões do Android (incluindo Android 13, 14, 15 e 16).
  - Adicionado suporte a múltiplos níveis de contingência (Apps Padrão, RoleManager com Activity, interfaces customizadas de fabricantes como MIUI/HyperOS e Informações do App) para máxima compatibilidade entre diferentes marcas e modelos.
