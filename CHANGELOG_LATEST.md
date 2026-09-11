### 🚀 Novidades e Melhorias da Versão 0.48.4

- **Download Confiável e Atualização Direta no App:**
  - Corrigido o botão de download dentro da launcher que não iniciava o download da nova build: substituído o `DownloadManager` padrão do Android por um mecanismo direto com coroutines em segundo plano.
  - Tratamento e resolução automática de redirecionamentos HTTP 302/307 do GitHub Releases até o asset final.
  - Gravação no armazenamento público via `MediaStore.Downloads` (Android 10+) com substituição limpa do arquivo sem duplicatas `(1).apk`, e suporte com `FileProvider` para compartilhamento seguro do APK.
  - Indicador de progresso de download em tempo real ("Baixando X%…") tanto no menu da tela inicial quanto dentro do balão flutuante de mudanças, mudando automaticamente para "Instalar" ao finalizar.
  - Adicionadas permissões `REQUEST_INSTALL_PACKAGES` e suporte ao instalador de pacotes para acionar a instalação diretamente.

- **Correção da Quebra de Linha nas Opções de Fonte:**
  - Corrigido o bug na tela de Configurações onde a última opção de fonte ("Monoespaçada") não cabia na largura da tela e não pulava para a linha de baixo.
  - Implementado layout adaptativo com `FlowRow` em todas as opções com chips múltiplos (tipo de fonte, cor do texto e alinhamento lateral), garantindo quebra suave para a linha seguinte em qualquer tamanho ou densidade de tela.

- **Tradução Completa para Português (Brasil - pt-BR / pt):**
  - Tradução integral de 100% dos textos, botões, opções e telas de configuração para o Português.
  - Adicionado suporte nativo de localização com diretórios `values-pt` e `values-pt-rBR` totalmente sincronizados (144 strings localizadas).
  - Tradução dos nomes e descrições dos serviços de sistema no Android (Gestos de Acessibilidade e Acesso a Notificações).
  - Remoção de todos os textos fixos (*hardcoded*) nas telas de opções, diálogo flutuante de mudanças e notificações de download.

- **Menu da Tela Inicial e Diálogo de Mudanças:**
  - Exibição de todas as ações e alertas em Português ("Opções", "Papel de parede", "Adicionar widget", "Mudanças", "Atualizar", "O que há de novo").
  - Diálogo de mudanças com efeito translúcido (*frosted glass*) e desfoque nativo (`FLAG_BLUR_BEHIND`).
  - Animação suave com gestos elásticos de arrastar para baixo para fechar.

