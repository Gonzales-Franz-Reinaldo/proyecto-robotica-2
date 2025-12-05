Instrucciones de Instalación y Ejecución

Paso 1: Instalar dependencias
cd /home/franz/workspace/projects/sistema-deteccion-somnolencia/
drowsiness-detecction-backend
Crear entorno virtual (si no existe)

python -m venv venv
Activar entorno virtual

source venv/bin/activate
Instalar dependencias

pip install -r requirements.txt
Paso 2: Configurar variables de entorno
Generar SECRET_KEY seguro

openssl rand -hex 32 -> Ejecutar en la terminal para generar el Api Key y pegar en .env
Editar .env con tus credenciales

nano .env

Contenido mínimo de .env:

DB_HOST=localhost DB_PORT=5432 DB_USER=postgres DB_PASSWORD=tu_password DB_NAME=sistema_deteccion_somnolencia

SECRET_KEY=a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6 # Generado con openssl ALGORITHM=HS256 ACCESS_TOKEN_EXPIRE_MINUTES=30 REFRESH_TOKEN_EXPIRE_DAYS=7

DEBUG=True ENVIRONMENT=development
Paso 3: Verificar conexión a la base de datos
Entrar a PostgreSQL

psql -U postgres -d sistema_deteccion_somnolencia
Verificar que existan las tablas

\dt
Verificar que exista al menos un usuario

SELECT usuario, rol, activo FROM usuarios;
Paso 4: Ejecutar la aplicación
Usar uvicorn

uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
Para que funcione el login con las credenciales válidas ejecutar el scrip test/regenerate_password

    python test/regenerate_passwords.py


# PARA LA ALARMA SONORA
# para detectar el dispositivo de alarma
- pip install tinytuya
- python -m tinytuya scan
- python -m tinytuya wizard

# Access ID/Client ID: 
- senmpkvtvguawcydwrxr
# Access Secret/Client Secret:
- 0943972feb654916ae352330e866a442