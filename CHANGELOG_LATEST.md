### 🚀 Novidades e Melhorias da Versão 0.59.14

- **Reversão das Modificações de Desfoque (Blur) na Lista de Aplicativos:**
  - Revertidas todas as alterações recentes e commits relacionados à implementação de blur na gaveta de aplicativos e no papel de parede.
  - Removido o `WallpaperBlurManager` e algoritmo de Stack Blur (`FastBlur`).
  - Restaurado o comportamento estável e original da lista de aplicativos com escurecimento de fundo via opacidade configurável (*Aparência → Escurecer papel de parede na lista de apps*).
  - Removida a opção de alternância de blur das configurações e recursos de texto correspondentes.
