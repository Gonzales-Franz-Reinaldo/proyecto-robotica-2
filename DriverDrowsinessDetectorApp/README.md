DriverDrowsinessDetectorApp/
│
├── app/
│   ├── build.gradle.kts                    # ✅ Ya configurado arriba
│   ├── proguard-rules.pro                  # ✅ Ya configurado arriba
│   │
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml         # ✅ Ya configurado arriba
│   │   │   │
│   │   │   ├── assets/                     # ⚠️ CREAR ESTA CARPETA
│   │   │   │   └── models/                 # Modelos MediaPipe
│   │   │   │       ├── face_landmarker.task   # ⬇️ Descargar de Google
│   │   │   │       └── hand_landmarker.task   # ⬇️ Opcional
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── ic_launcher_background.xml  # ✅ Ya existe
│   │   │   │   │   ├── ic_launcher_foreground.xml  # ✅ Ya existe
│   │   │   │   │   ├── ic_alert.xml                # 🆕 Crear (icono alerta)
│   │   │   │   │   ├── ic_eye.xml                  # 🆕 Crear (icono ojo)
│   │   │   │   │   ├── ic_gps.xml                  # 🆕 Crear (icono GPS)
│   │   │   │   │   └── ic_sync.xml                 # 🆕 Crear (icono sync)
│   │   │   │   │
│   │   │   │   ├── raw/                            # 🆕 CREAR CARPETA
│   │   │   │   │   ├── alert_low.mp3               # 🆕 Sonido alerta baja
│   │   │   │   │   ├── alert_medium.mp3            # 🆕 Sonido alerta media
│   │   │   │   │   └── alert_critical.mp3          # 🆕 Sonido alerta crítica
│   │   │   │   │
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml                 # 🆕 Actualizar
│   │   │   │   │   ├── colors.xml                  # ✅ Ya existe
│   │   │   │   │   └── themes.xml                  # ✅ Ya existe
│   │   │   │   │
│   │   │   │   └── xml/
│   │   │   │       ├── backup_rules.xml            # ✅ Ya existe
│   │   │   │       └── data_extraction_rules.xml   # ✅ Ya existe
│   │   │   │
│   │   │   └── kotlin/com/example/driverdrowsinessdetectorapp/
│   │   │       │
│   │   │       ├── DriversDetectorApp.kt           # 🆕 Application class con @HiltAndroidApp
│   │   │       │
│   │   │       ├── di/                             # 🆕 DEPENDENCY INJECTION
│   │   │       │   ├── AppModule.kt                # Provee Retrofit, OkHttp, Moshi
│   │   │       │   ├── DatabaseModule.kt           # Provee Room Database
│   │   │       │   ├── RepositoryModule.kt         # Provee Repositorios
│   │   │       │   ├── UseCaseModule.kt            # Provee UseCases
│   │   │       │   └── NetworkModule.kt            # Provee interceptors, network checker
│   │   │       │
│   │   │       ├── data/                           # 🆕 CAPA DE DATOS
│   │   │       │   │
│   │   │       │   ├── local/                      # Base de datos local (Room)
│   │   │       │   │   ├── dao/                    # Data Access Objects
│   │   │       │   │   │   ├── AlertDao.kt
│   │   │       │   │   │   ├── SessionDao.kt
│   │   │       │   │   │   ├── LocationDao.kt
│   │   │       │   │   │   └── MetricsDao.kt
│   │   │       │   │   │
│   │   │       │   │   ├── database/
│   │   │       │   │   │   └── AppDatabase.kt      # @Database(entities = [...])
│   │   │       │   │   │
│   │   │       │   │   └── entity/                 # Entidades Room (Tablas)
│   │   │       │   │       ├── AlertEntity.kt
│   │   │       │   │       ├── SessionEntity.kt
│   │   │       │   │       ├── LocationEntity.kt
│   │   │       │   │       └── MetricsEntity.kt
│   │   │       │   │
│   │   │       │   ├── remote/                     # API Cloud (Retrofit)
│   │   │       │   │   ├── api/                    # Interfaces API
│   │   │       │   │   │   ├── AuthApi.kt          # Login, Logout, Me
│   │   │       │   │   │   ├── SyncApi.kt          # Sincronización de datos
│   │   │       │   │   │   └── SessionApi.kt       # CRUD de sesiones
│   │   │       │   │   │
│   │   │       │   │   ├── dto/                    # Data Transfer Objects (JSON)
│   │   │       │   │   │   ├── request/
│   │   │       │   │   │   │   ├── LoginRequest.kt
│   │   │       │   │   │   │   ├── SyncRequest.kt
│   │   │       │   │   │   │   └── SessionRequest.kt
│   │   │       │   │   │   │
│   │   │       │   │   │   └── response/
│   │   │       │   │   │       ├── LoginResponse.kt
│   │   │       │   │   │       ├── SyncResponse.kt
│   │   │       │   │   │       ├── SessionResponse.kt
│   │   │       │   │   │       └── UserResponse.kt
│   │   │       │   │   │
│   │   │       │   │   ├── interceptor/            # Interceptors HTTP
│   │   │       │   │   │   ├── AuthInterceptor.kt  # JWT token injection
│   │   │       │   │   │   └── LoggingInterceptor.kt
│   │   │       │   │   │
│   │   │       │   │   └── mapper/                 # Mappers (DTO → Domain)
│   │   │       │   │       ├── UserMapper.kt
│   │   │       │   │       ├── AlertMapper.kt
│   │   │       │   │       └── SessionMapper.kt
│   │   │       │   │
│   │   │       │   ├── preferences/                # DataStore (Preferencias)
│   │   │       │   │   └── PreferencesManager.kt   # Token, userId, settings
│   │   │       │   │
│   │   │       │   └── repository/                 # Implementaciones de Repos
│   │   │       │       ├── AuthRepositoryImpl.kt
│   │   │       │       ├── SessionRepositoryImpl.kt
│   │   │       │       ├── AlertRepositoryImpl.kt
│   │   │       │       ├── LocationRepositoryImpl.kt
│   │   │       │       └── SyncRepositoryImpl.kt
│   │   │       │
│   │   │       ├── domain/                         # 🆕 CAPA DE DOMINIO (Lógica de Negocio)
│   │   │       │   │
│   │   │       │   ├── model/                      # Entidades de negocio
│   │   │       │   │   ├── User.kt
│   │   │       │   │   ├── Alert.kt
│   │   │       │   │   ├── AlertType.kt            # enum (BLINK, YAWN, MICROSLEEP, etc.)
│   │   │       │   │   ├── Session.kt
│   │   │       │   │   ├── SessionStatus.kt        # enum (ACTIVE, PAUSED, FINISHED)
│   │   │       │   │   ├── Location.kt
│   │   │       │   │   ├── MetricasSomnolencia.kt  # data class con métricas
│   │   │       │   │   └── SyncStatus.kt           # enum (PENDING, SYNCED, FAILED)
│   │   │       │   │
│   │   │       │   ├── repository/                 # Interfaces (Contratos)
│   │   │       │   │   ├── AuthRepository.kt
│   │   │       │   │   ├── SessionRepository.kt
│   │   │       │   │   ├── AlertRepository.kt
│   │   │       │   │   ├── LocationRepository.kt
│   │   │       │   │   └── SyncRepository.kt
│   │   │       │   │
│   │   │       │   └── usecase/                    # Casos de Uso (Lógica de Negocio)
│   │   │       │       ├── auth/
│   │   │       │       │   ├── LoginUseCase.kt
│   │   │       │       │   ├── LogoutUseCase.kt
│   │   │       │       │   ├── GetCurrentUserUseCase.kt
│   │   │       │       │   └── ValidateTokenUseCase.kt
│   │   │       │       │
│   │   │       │       ├── session/
│   │   │       │       │   ├── StartSessionUseCase.kt
│   │   │       │       │   ├── EndSessionUseCase.kt
│   │   │       │       │   ├── PauseSessionUseCase.kt
│   │   │       │       │   └── GetActiveSessionUseCase.kt
│   │   │       │       │
│   │   │       │       ├── monitoring/
│   │   │       │       │   ├── DetectDrowsinessUseCase.kt     # ⭐ CORE: Lógica IA
│   │   │       │       │   ├── CalculateEARUseCase.kt         # Eye Aspect Ratio
│   │   │       │       │   ├── CalculateMARUseCase.kt         # Mouth Aspect Ratio
│   │   │       │       │   ├── DetectHeadPoseUseCase.kt
│   │   │       │       │   └── ProcessFrameUseCase.kt
│   │   │       │       │
│   │   │       │       ├── alert/
│   │   │       │       │   ├── TriggerAlertUseCase.kt
│   │   │       │       │   ├── SaveAlertUseCase.kt
│   │   │       │       │   └── GetAlertsHistoryUseCase.kt
│   │   │       │       │
│   │   │       │       ├── location/
│   │   │       │       │   ├── TrackLocationUseCase.kt
│   │   │       │       │   ├── SaveLocationUseCase.kt
│   │   │       │       │   └── GetCurrentLocationUseCase.kt
│   │   │       │       │
│   │   │       │       └── sync/
│   │   │       │           ├── SyncDataUseCase.kt
│   │   │       │           ├── CheckConnectivityUseCase.kt
│   │   │       │           └── GetPendingSyncDataUseCase.kt
│   │   │       │
│   │   │       ├── presentation/                   # 🆕 CAPA DE PRESENTACIÓN (UI)
│   │   │       │   │
│   │   │       │   ├── main/                       # MainActivity y navegación
│   │   │       │   │   ├── ui/
│   │   │       │   │   │   ├── MainActivity.kt     # ✅ Ya existe (actualizar)
│   │   │       │   │   │   ├── MainScreen.kt       # Scaffold con NavHost
│   │   │       │   │   │   │
│   │   │       │   │   │   └── theme/              # ✅ Ya existe
│   │   │       │   │   │       ├── Color.kt
│   │   │       │   │   │       ├── Theme.kt
│   │   │       │   │   │       └── Type.kt
│   │   │       │   │   │
│   │   │       │   │   ├── MainViewModel.kt        # Estado global de nav
│   │   │       │   │   └── navigation/
│   │   │       │   │       ├── NavGraph.kt         # Rutas de navegación
│   │   │       │   │       └── Screen.kt           # sealed class con rutas
│   │   │       │   │
│   │   │       │   ├── auth/                       # Feature: Autenticación
│   │   │       │   │   ├── ui/
│   │   │       │   │   │   ├── LoginScreen.kt      # Composable
│   │   │       │   │   │   └── components/
│   │   │       │   │   │       ├── LoginForm.kt
│   │   │       │   │   │       └── LoginButton.kt
│   │   │       │   │   │
│   │   │       │   │   ├── LoginViewModel.kt       # @HiltViewModel
│   │   │       │   │   └── LoginUiState.kt         # sealed class
│   │   │       │   │
│   │   │       │   ├── dashboard/                  # Feature: Dashboard
│   │   │       │   │   ├── ui/
│   │   │       │   │   │   ├── DashboardScreen.kt
│   │   │       │   │   │   └── components/
│   │   │       │   │   │       ├── SessionCard.kt
│   │   │       │   │   │       ├── StatsCard.kt
│   │   │       │   │   │       └── StartButton.kt
│   │   │       │   │   │
│   │   │       │   │   ├── DashboardViewModel.kt
│   │   │       │   │   └── DashboardUiState.kt
│   │   │       │   │
│   │   │       │   ├── monitoring/                 # Feature: Monitoreo (⭐ CORE)
│   │   │       │   │   ├── ui/
│   │   │       │   │   │   ├── MonitoringScreen.kt # Pantalla principal de IA
│   │   │       │   │   │   └── components/
│   │   │       │   │   │       ├── CameraPreview.kt    # CameraX preview
│   │   │       │   │   │       ├── FaceLandmarks.kt    # Dibuja puntos faciales
│   │   │       │   │   │       ├── MetricsDisplay.kt   # Muestra métricas
│   │   │       │   │   │       ├── AlertBanner.kt      # Banner de alerta
│   │   │       │   │   │       └── SessionControls.kt  # Play/Pause/Stop
│   │   │       │   │   │
│   │   │       │   │   ├── MonitoringViewModel.kt  # Lógica de monitoreo
│   │   │       │   │   ├── MonitoringUiState.kt
│   │   │       │   │   │
│   │   │       │   │   └── service/                # Foreground Service
│   │   │       │   │       ├── AlertService.kt     # Service que corre en foreground
│   │   │       │   │       ├── AlertServiceBinder.kt
│   │   │       │   │       └── AlertNotification.kt
│   │   │       │   │
│   │   │       │   ├── history/                    # Feature: Historial
│   │   │       │   │   ├── ui/
│   │   │       │   │   │   ├── HistoryScreen.kt
│   │   │       │   │   │   ├── SessionDetailScreen.kt
│   │   │       │   │   │   └── components/
│   │   │       │   │   │       ├── SessionListItem.kt
│   │   │       │   │   │       ├── AlertTimeline.kt
│   │   │       │   │   │       └── RouteMap.kt
│   │   │       │   │   │
│   │   │       │   │   ├── HistoryViewModel.kt
│   │   │       │   │   └── HistoryUiState.kt
│   │   │       │   │
│   │   │       │   └── settings/                   # Feature: Configuración
│   │   │       │       ├── ui/
│   │   │       │       │   ├── SettingsScreen.kt
│   │   │       │       │   └── components/
│   │   │       │       │       ├── SensitivitySlider.kt
│   │   │       │       │       ├── SyncIntervalPicker.kt
│   │   │       │       │       └── LogoutButton.kt
│   │   │       │       │
│   │   │       │       ├── SettingsViewModel.kt
│   │   │       │       └── SettingsUiState.kt
│   │   │       │
│   │   │       ├── util/                           # 🆕 UTILIDADES
│   │   │       │   ├── Constants.kt                # Constantes globales
│   │   │       │   ├── Extensions.kt               # Extension functions
│   │   │       │   ├── NetworkUtil.kt              # Check conectividad
│   │   │       │   ├── PermissionsUtil.kt          # Helper de permisos
│   │   │       │   ├── DateTimeUtil.kt             # Formateo de fechas
│   │   │       │   └── MediaPipeUtil.kt            # Helpers para MediaPipe
│   │   │       │
│   │   │       └── worker/                         # 🆕 BACKGROUND WORKERS
│   │   │           ├── SyncWorker.kt               # WorkManager para sync
│   │   │           └── CleanupWorker.kt            # Limpiar datos antiguos
│   │   │
│   │   ├── test/                                   # 🆕 UNIT TESTS
│   │   │   └── kotlin/com/example/driverdrowsinessdetectorapp/
│   │   │       ├── domain/usecase/
│   │   │       │   ├── DetectDrowsinessUseCaseTest.kt
│   │   │       │   ├── CalculateEARUseCaseTest.kt
│   │   │       │   └── LoginUseCaseTest.kt
│   │   │       │
│   │   │       ├── data/repository/
│   │   │       │   └── SessionRepositoryTest.kt
│   │   │       │
│   │   │       └── presentation/
│   │   │           └── monitoring/MonitoringViewModelTest.kt
│   │   │
│   │   └── androidTest/                            # 🆕 INSTRUMENTED TESTS
│   │       └── kotlin/com/example/driverdrowsinessdetectorapp/
│   │           ├── data/local/
│   │           │   └── AppDatabaseTest.kt
│   │           │
│   │           └── presentation/
│   │               ├── LoginScreenTest.kt
│   │               └── MonitoringScreenTest.kt
│   │
│   └── schemas/                                    # 🆕 Room schemas (auto-generado)
│
├── gradle/
│   ├── libs.versions.toml                          # ✅ Ya configurado arriba
│   └── wrapper/
│       └── gradle-wrapper.properties
│
├── .gitignore
├── build.gradle.kts                                # Root build file
├── gradle.properties
├── gradlew
├── gradlew.bat
├── local.properties
├── README.md                                       # ✅ Ya existe
└── settings.gradle.kts