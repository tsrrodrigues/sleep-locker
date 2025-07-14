# Sleep Locker

Um aplicativo Android que detecta quando o usuário adormece (via Mi Band 9) e bloqueia automaticamente a reprodução de mídia e a tela do dispositivo.

## Sobre

O Sleep Locker é um aplicativo inteligente que monitora o sono do usuário através de uma Mi Band 9 conectada via Bluetooth LE. Quando detecta que o usuário adormeceu, o app pausa automaticamente a reprodução de mídia e bloqueia a tela do dispositivo.

### Funcionalidades

- **Detecção de Sono**: Algoritmo Cole-Kripke simplificado que analisa frequência cardíaca e movimento
- **Bloqueio Automático**: Pausa mídia e bloqueia tela quando detecta sono profundo
- **Serviço em Primeiro Plano**: Monitoramento contínuo com notificação persistente
- **Configuração de Delay**: Delay configurável antes do bloqueio (0-10 minutos)
- **Modo Pro**: Upgrade único que remove anúncios e habilita recursos extras
- **Economia de Bateria**: Desconecta BLE quando bateria < 15%

## Arquitetura

### Tech Stack

- **Linguagem**: Kotlin 1.9
- **UI**: Jetpack Compose + Material 3
- **DI**: Hilt
- **BLE**: Nordic Semiconductor BLE Library
- **Billing**: Google Play Billing v6
- **Ads**: AdMob SDK 22+

### Estrutura do Projeto

```
app/src/main/java/com/tsrrodrigues/sleeplocker/
├── domain/
│   ├── model/          # Modelos de dados
│   ├── sleep/          # Detector de sono
│   └── lock/           # Controlador de bloqueio
├── service/            # Serviço de monitoramento
├── device/             # Device Admin
├── ui/                 # Interface do usuário
│   ├── screen/         # Telas
│   ├── viewmodel/      # ViewModels
│   └── theme/          # Temas e estilos
└── util/               # Utilitários
```

## Como Buildar

### Pré-requisitos

- Android Studio Hedgehog ou superior
- JDK 17
- Android SDK 34
- Dispositivo Android com API 26+ (Android 8.0+)

### Passos

1. **Clone o repositório**

   ```bash
   git clone https://github.com/tsrrodrigues/sleep-locker.git
   cd sleep-locker
   ```

2. **Abra no Android Studio**

   - Abra o Android Studio
   - Selecione "Open an existing project"
   - Navegue até a pasta do projeto e selecione

3. **Configure as dependências**

   - O projeto já está configurado com todas as dependências necessárias
   - Sincronize o projeto (File > Sync Project with Gradle Files)

4. **Build e Execute**

   ```bash
   # Via linha de comando
   ./gradlew assembleDebug

   # Ou via Android Studio
   # Clique em "Run" (ícone de play verde)
   ```

### Configurações Adicionais

#### AdMob (Opcional)

Para usar anúncios reais, substitua o App ID de teste no `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="SEU_APP_ID_AQUI" />
```

#### Google Play Billing (Opcional)

Para testar compras, adicione seu SKU no código:

```kotlin
private const val PRO_SKU = "sleep_lock_pro"
```

## Permissões Necessárias

O app solicita as seguintes permissões:

- **BLUETOOTH_CONNECT/SCAN**: Para conectar com Mi Band
- **ACCESS_FINE_LOCATION**: Necessário para BLE no Android 12+
- **MEDIA_CONTENT_CONTROL**: Para pausar mídia
- **USE_FULL_SCREEN_INTENT**: Para notificações
- **Device Admin**: Para bloquear tela

## Testes

### Testes Unitários

```bash
./gradlew test
```

### Testes de Instrumentação

```bash
./gradlew connectedCheck
```

### Testes Manuais

- Dispositivo Xiaomi MIUI
- Samsung One UI
- Pixel AOSP

## Roadmap

### v1.1 (Próximas versões)

- Integração com Philips Hue
- Suporte a Alexa
- Novos wearables (Apple Watch, Galaxy Watch)
- Modo offline
- Backup de configurações

## Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## Suporte

Para suporte, envie um email para [seu-email@exemplo.com] ou abra uma issue no GitHub.

---

**Nota**: Este é um projeto em desenvolvimento. Algumas funcionalidades podem estar em implementação.
