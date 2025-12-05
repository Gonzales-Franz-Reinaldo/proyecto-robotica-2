-- ============================================
-- SISTEMA DE DETECCIÓN DE SOMNOLENCIA
-- Base de Datos PostgreSQL - Versión Refactorizada (CORREGIDA)
-- ============================================

-- ============================================
-- 1. TABLA: empresas
-- Empresas de transporte (opcional para choferes)
-- ============================================
CREATE TABLE empresas (
    id_empresa SERIAL PRIMARY KEY,
    nombre_empresa VARCHAR(200) NOT NULL UNIQUE,
    ruc VARCHAR(20) UNIQUE,
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 2. TABLA: usuarios
-- Usuarios del sistema (ADMIN y CHOFER)
-- ============================================
CREATE TABLE usuarios (
    id_usuario SERIAL PRIMARY KEY,
    
    -- Credenciales (Compartidas por ambos roles)
    usuario VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) CHECK (rol IN ('admin', 'chofer')) NOT NULL,
    
    -- Datos Personales (Completos para CHOFER, básicos para ADMIN)
    nombre_completo VARCHAR(200) NOT NULL,
    dni_ci VARCHAR(20) UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    
    -- Datos adicionales para CHOFER (NULL para admin)
    genero VARCHAR(20) CHECK (genero IN ('masculino', 'femenino', 'otro')),
    nacionalidad VARCHAR(100),
    fecha_nacimiento DATE,
    direccion TEXT,
    ciudad VARCHAR(100),
    codigo_postal VARCHAR(20),
    
    -- Información Laboral (Solo para CHOFER)
    tipo_chofer VARCHAR(20) CHECK (tipo_chofer IN ('individual', 'empresa')),
    id_empresa INTEGER REFERENCES empresas(id_empresa) ON DELETE SET NULL,
    numero_licencia VARCHAR(50),
    categoria_licencia VARCHAR(50),
    
    -- Estado y Control
    activo BOOLEAN DEFAULT TRUE,
    primer_inicio BOOLEAN DEFAULT TRUE, -- Para forzar cambio de contraseña
    
    -- Metadatos
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultima_sesion TIMESTAMP,
    
    -- Validaciones
    CONSTRAINT chk_chofer_empresa CHECK (
        (rol = 'chofer' AND tipo_chofer = 'empresa' AND id_empresa IS NOT NULL) OR
        (rol = 'chofer' AND tipo_chofer = 'individual') OR
        (rol = 'admin')
    ),
    CONSTRAINT chk_chofer_datos CHECK (
        (rol = 'chofer' AND dni_ci IS NOT NULL AND genero IS NOT NULL AND tipo_chofer IS NOT NULL) OR
        (rol = 'admin')
    )
);


-- ============================================
-- TABLA: token_blacklist
-- Tokens JWT invalidados (logout)
-- ============================================
CREATE TABLE token_blacklist (
    id SERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    id_usuario INTEGER REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    fecha_invalidacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP NOT NULL
);

-- Índice para búsqueda rápida
CREATE INDEX idx_token_blacklist_token ON token_blacklist(token);
CREATE INDEX idx_token_blacklist_expiracion ON token_blacklist(fecha_expiracion);

-- Comentario
COMMENT ON TABLE token_blacklist IS 'Tokens JWT invalidados por logout o revocación manual';


-- ============================================
-- 3. TABLA: sesiones_viaje
-- Registro de cada sesión de monitoreo
-- ============================================
CREATE TABLE sesiones_viaje (
    id_sesion SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    
    -- Información temporal
    fecha_inicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP,
    duracion_minutos INTEGER,
    
    -- Ubicación
    ruta_nombre VARCHAR(200),
    ubicacion_inicio VARCHAR(300),
    ubicacion_fin VARCHAR(300),
    
    -- Estado de la sesión
    estado VARCHAR(20) CHECK (estado IN ('activa', 'finalizada', 'interrumpida')) DEFAULT 'activa',
    nivel_alerta VARCHAR(20) CHECK (nivel_alerta IN ('normal', 'alerta', 'critico')) DEFAULT 'normal',
    
    -- Contadores de eventos
    total_microsueños INTEGER DEFAULT 0,
    total_bostezos INTEGER DEFAULT 0,
    total_parpadeos_excesivos INTEGER DEFAULT 0,
    total_cabeceos INTEGER DEFAULT 0,
    total_frotamiento_ojos INTEGER DEFAULT 0,
    
    -- Notas adicionales
    observaciones TEXT
);

-- ============================================
-- 4. TABLA: alertas_somnolencia
-- Eventos de somnolencia detectados durante las sesiones
-- ============================================
CREATE TABLE alertas_somnolencia (
    id_alerta SERIAL PRIMARY KEY,
    id_sesion INTEGER NOT NULL REFERENCES sesiones_viaje(id_sesion) ON DELETE CASCADE,
    
    -- Información del evento
    timestamp_alerta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_alerta VARCHAR(50) CHECK (tipo_alerta IN (
        'microsueño',
        'bostezo',
        'parpadeo_excesivo',
        'cabeceo',
        'frotamiento_ojos'
    )) NOT NULL,
    
    -- Severidad del evento
    severidad VARCHAR(20) CHECK (severidad IN ('leve', 'moderado', 'grave', 'critico')) NOT NULL,
    
    -- Duración del evento en segundos
    duracion_segundos DECIMAL(5, 2),
    
    -- Datos adicionales del evento (métricas específicas)
    detalles JSONB,
    
    -- Acción tomada por el sistema
    accion_tomada VARCHAR(200)
);

-- ============================================
-- 5. TABLA: metricas_sesion
-- Métricas y estadísticas calculadas por sesión
-- ============================================
CREATE TABLE metricas_sesion (
    id_metrica SERIAL PRIMARY KEY,
    id_sesion INTEGER NOT NULL REFERENCES sesiones_viaje(id_sesion) ON DELETE CASCADE,
    
    -- Timestamp de la métrica
    timestamp_metrica TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Métricas de ojos
    distancia_ojo_izq DECIMAL(6, 3),
    distancia_ojo_der DECIMAL(6, 3),
    promedio_distancia_ojos DECIMAL(6, 3),
    
    -- Métricas de boca
    apertura_bucal DECIMAL(6, 3),
    
    -- Métricas de cabeza (ángulos en grados)
    angulo_pitch DECIMAL(6, 2), -- Cabeceo (arriba/abajo)
    angulo_yaw DECIMAL(6, 2),   -- Rotación (izq/der)
    angulo_roll DECIMAL(6, 2),  -- Inclinación lateral
    
    -- Estado general en ese momento
    estado_momento VARCHAR(20) CHECK (estado_momento IN ('normal', 'alerta', 'critico'))
);

-- ============================================
-- 6. TABLA: ubicaciones_gps
-- Tracking GPS durante el viaje
-- ============================================
CREATE TABLE ubicaciones_gps (
    id_ubicacion SERIAL PRIMARY KEY,
    id_sesion INTEGER NOT NULL REFERENCES sesiones_viaje(id_sesion) ON DELETE CASCADE,
    
    -- Coordenadas GPS
    timestamp_gps TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    latitud DECIMAL(10, 8) NOT NULL,
    longitud DECIMAL(11, 8) NOT NULL,
    
    -- Velocidad
    velocidad_kmh DECIMAL(6, 2),
    
    -- Dirección textual (geocodificación inversa)
    direccion VARCHAR(300),
    zona VARCHAR(150)
);

-- ============================================
-- 7. TABLA: configuracion_usuario
-- Configuraciones personalizables por usuario (solo choferes)
-- ============================================
CREATE TABLE configuracion_usuario (
    id_config SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL UNIQUE REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    
    -- Parámetros de detección
    sensibilidad_deteccion INTEGER CHECK (sensibilidad_deteccion BETWEEN 1 AND 10) DEFAULT 5,
    umbral_microsueño_seg DECIMAL(4, 2) DEFAULT 2.5,
    
    -- Alertas
    alertas_sonoras BOOLEAN DEFAULT TRUE,
    alertas_visuales BOOLEAN DEFAULT TRUE,
    
    -- Notificaciones
    notificaciones_email BOOLEAN DEFAULT FALSE,
    email_notificacion VARCHAR(100),
    
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    
);

-- ============================================
-- 8. TABLA: reportes
-- Metadata de reportes generados
-- ============================================
CREATE TABLE reportes (
    id_reporte SERIAL PRIMARY KEY,
    
    -- Usuario que generó el reporte (admin)
    id_usuario_generador INTEGER REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    
    -- Relacionado con (puede ser de una sesión específica o general)
    id_sesion INTEGER REFERENCES sesiones_viaje(id_sesion) ON DELETE SET NULL,
    id_usuario_chofer INTEGER REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    id_empresa INTEGER REFERENCES empresas(id_empresa) ON DELETE SET NULL,
    
    -- Información del reporte
    tipo_reporte VARCHAR(50) CHECK (tipo_reporte IN ('sesion', 'chofer', 'empresa', 'general')) NOT NULL,
    fecha_generacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    periodo_inicio DATE,
    periodo_fin DATE,
    
    -- Archivo generado
    nombre_archivo VARCHAR(300) NOT NULL,
    ruta_archivo VARCHAR(500) NOT NULL,
    formato VARCHAR(10) CHECK (formato IN ('PDF', 'CSV', 'XLSX')) DEFAULT 'PDF',
    tamaño_kb INTEGER
);

-- ============================================
-- 9. TABLA: viajes
-- Asignación de viajes/rutas a choferes
-- ============================================
CREATE TABLE viajes (
    id_viaje SERIAL PRIMARY KEY,
    
    -- Relaciones (solo choferes pueden ser asignados)
    id_chofer INTEGER NOT NULL REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    id_empresa INTEGER NOT NULL REFERENCES empresas(id_empresa) ON DELETE CASCADE,
    
    -- Información de la ruta
    origen VARCHAR(100) NOT NULL,
    destino VARCHAR(100) NOT NULL,
    duracion_estimada VARCHAR(50) NOT NULL,
    distancia_km DECIMAL(8, 2),
    
    -- Estado del viaje
    estado VARCHAR(20) CHECK (estado IN ('pendiente', 'en_curso', 'completada', 'cancelada')) DEFAULT 'pendiente',
    
    -- Fechas
    fecha_asignacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_inicio TIMESTAMP,
    fecha_fin TIMESTAMP,
    
    -- Notas adicionales
    observaciones TEXT,
    
    -- Validaciones
    CONSTRAINT chk_viaje_origen_destino CHECK (origen != destino)
    
    -- *** SE ELIMINA LA RESTRICCIÓN CHECK chk_viaje_chofer_rol QUE CAUSÓ EL ERROR ***
    -- La validación inter-tabla se implementa mediante un TRIGGER (ver más abajo)
);

-- ============================================
-- 🌟 FUNCIÓN Y TRIGGER PARA CORREGIR EL ERROR EN viajes
-- (Valida que el id_chofer tenga el rol 'chofer')
-- ============================================

-- 1. Función para verificar que el ID de chofer sea realmente un chofer
CREATE OR REPLACE FUNCTION verificar_chofer_rol()
RETURNS TRIGGER AS $$
BEGIN
    -- Busca el rol del usuario asignado
    IF (SELECT rol FROM usuarios WHERE id_usuario = NEW.id_chofer) != 'chofer' THEN
        -- Si el rol no es 'chofer', lanza una excepción y aborta la operación
        RAISE EXCEPTION 'Error de asignación: El usuario con ID % no tiene el rol ''chofer''.', NEW.id_chofer;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Crear el trigger en la tabla viajes
CREATE TRIGGER trg_verificar_chofer_rol
BEFORE INSERT OR UPDATE OF id_chofer ON viajes
FOR EACH ROW
EXECUTE FUNCTION verificar_chofer_rol();

-- ============================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- ============================================

-- Índices en usuarios
CREATE INDEX idx_usuarios_rol ON usuarios(rol);
CREATE INDEX idx_usuarios_empresa ON usuarios(id_empresa);
CREATE INDEX idx_usuarios_tipo_chofer ON usuarios(tipo_chofer);
CREATE INDEX idx_usuarios_activo ON usuarios(activo);
CREATE INDEX idx_usuarios_dni ON usuarios(dni_ci);

-- Índices en sesiones_viaje
CREATE INDEX idx_sesiones_usuario ON sesiones_viaje(id_usuario);
CREATE INDEX idx_sesiones_estado ON sesiones_viaje(estado);
CREATE INDEX idx_sesiones_fecha ON sesiones_viaje(fecha_inicio DESC);
CREATE INDEX idx_sesiones_nivel_alerta ON sesiones_viaje(nivel_alerta);

-- Índices en alertas_somnolencia
CREATE INDEX idx_alertas_sesion ON alertas_somnolencia(id_sesion);
CREATE INDEX idx_alertas_tipo ON alertas_somnolencia(tipo_alerta);
CREATE INDEX idx_alertas_timestamp ON alertas_somnolencia(timestamp_alerta DESC);
CREATE INDEX idx_alertas_severidad ON alertas_somnolencia(severidad);

-- Índices en metricas_sesion
CREATE INDEX idx_metricas_sesion ON metricas_sesion(id_sesion);
CREATE INDEX idx_metricas_timestamp ON metricas_sesion(timestamp_metrica DESC);

-- Índices en ubicaciones_gps
CREATE INDEX idx_ubicaciones_sesion ON ubicaciones_gps(id_sesion);
CREATE INDEX idx_ubicaciones_timestamp ON ubicaciones_gps(timestamp_gps DESC);
CREATE INDEX idx_ubicaciones_coords ON ubicaciones_gps(latitud, longitud);

-- Índices en reportes
CREATE INDEX idx_reportes_tipo ON reportes(tipo_reporte);
CREATE INDEX idx_reportes_fecha ON reportes(fecha_generacion DESC);
CREATE INDEX idx_reportes_generador ON reportes(id_usuario_generador);

-- Índices en viajes
CREATE INDEX idx_viajes_chofer ON viajes(id_chofer);
CREATE INDEX idx_viajes_empresa ON viajes(id_empresa);
CREATE INDEX idx_viajes_estado ON viajes(estado);
CREATE INDEX idx_viajes_fecha_asignacion ON viajes(fecha_asignacion DESC);
CREATE INDEX idx_viajes_origen ON viajes(origen);
CREATE INDEX idx_viajes_destino ON viajes(destino);

-- ============================================
-- COMENTARIOS EN TABLAS
-- ============================================

COMMENT ON TABLE empresas IS 'Empresas de transporte que contratan choferes';
COMMENT ON TABLE usuarios IS 'Usuarios del sistema con rol admin o chofer';
COMMENT ON TABLE sesiones_viaje IS 'Sesiones de monitoreo activas o finalizadas';
COMMENT ON TABLE alertas_somnolencia IS 'Eventos de somnolencia detectados en tiempo real';
COMMENT ON TABLE metricas_sesion IS 'Métricas faciales y de comportamiento capturadas';
COMMENT ON TABLE ubicaciones_gps IS 'Tracking GPS durante las sesiones de viaje';
COMMENT ON TABLE configuracion_usuario IS 'Configuraciones personalizadas por chofer';
COMMENT ON TABLE reportes IS 'Metadata de reportes PDF/CSV generados';
COMMENT ON TABLE viajes IS 'Asignación de viajes/rutas a choferes con información de origen y destino';

-- ============================================
-- DATOS INICIALES
-- ============================================

-- Empresas de ejemplo
INSERT INTO empresas (nombre_empresa, ruc, telefono, email, direccion)
VALUES 
    ('TransCorp SA', '1234567890', '+591 4-4123456', 'contacto@transcorp.com', 'Av. América #123, Cochabamba'),
    ('FleetPro', '0987654321', '+591 2-2987654', 'info@fleetpro.com', 'Calle Comercio #456, La Paz'),
    ('Expreso del Sur', '1122334455', '+591 3-3445566', 'info@expresodelsur.com', 'Av. Montes #789, Santa Cruz');

-- Usuario ADMIN por defecto
-- Usuario: admin | Password: admin123 (Hash: $2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5L2xOVLcKKVIa)
INSERT INTO usuarios (usuario, password_hash, rol, nombre_completo, email, telefono, activo, primer_inicio)
VALUES ('admin', '$2b$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5L2xOVLcKKVIa', 'admin', 'Administrador del Sistema', 'admin@sistema.com', '+591 70000000', TRUE, FALSE);

-- Chofer INDIVIDUAL de ejemplo
-- Usuario: jperez | Password: chofer123 (Hash: $2b$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi)
INSERT INTO usuarios (
    usuario, password_hash, rol, nombre_completo, dni_ci, email, telefono,
    genero, nacionalidad, fecha_nacimiento, direccion, ciudad, codigo_postal,
    tipo_chofer, numero_licencia, categoria_licencia, activo, primer_inicio
)
VALUES (
    'jperez', '$2b$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'chofer', 
    'Juan Carlos Pérez', '12345678', 'juan.perez@email.com', '+591 70123456',
    'masculino', 'Boliviana', '1985-03-15', 'Av. Principal #123', 'La Paz', '00000',
    'individual', 'LIC-123456', 'Categoría C - Vehículos Pesados', TRUE, TRUE
);

-- Chofer de EMPRESA de ejemplo
-- Usuario: mlopez | Password: chofer123 (Hash: $2b$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi)
INSERT INTO usuarios (
    usuario, password_hash, rol, nombre_completo, dni_ci, email, telefono,
    genero, nacionalidad, fecha_nacimiento, direccion, ciudad, codigo_postal,
    tipo_chofer, id_empresa, numero_licencia, categoria_licencia, activo, primer_inicio
)
VALUES (
    'mlopez', '$2b$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'chofer',
    'María López González', '87654321', 'maria.lopez@transcorp.com', '+591 70987654',
    'femenino', 'Boliviana', '1990-07-22', 'Calle Comercio #456', 'Cochabamba', '00001',
    'empresa', 1, 'LIC-789012', 'Categoría D - Transporte Público', TRUE, TRUE
);

-- Configuración por defecto para los choferes
INSERT INTO configuracion_usuario (id_usuario) 
SELECT id_usuario FROM usuarios WHERE rol = 'chofer';

-- Viaje de ejemplo (usando el chofer de la empresa 1)
INSERT INTO viajes (id_chofer, id_empresa, origen, destino, duracion_estimada, distancia_km, estado, fecha_inicio)
VALUES (3, 1, 'Cochabamba', 'Santa Cruz', '8 horas', 500.50, 'en_curso', CURRENT_TIMESTAMP);