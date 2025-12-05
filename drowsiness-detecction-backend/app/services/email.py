# ============================================
# SERVICIO DE ENVÍO DE EMAILS
# Maneja el envío de correos electrónicos usando SMTP
# ============================================

import smtplib
import logging
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from typing import Optional
from datetime import datetime

from app.core.config import settings

# Configurar logger
logger = logging.getLogger(__name__)


class EmailService:
    """
    Servicio para envío de emails usando SMTP (Gmail)
    """
    
    def __init__(self):
        self.smtp_host = settings.SMTP_HOST
        self.smtp_port = settings.SMTP_PORT
        self.smtp_user = settings.SMTP_USER
        self.smtp_password = settings.SMTP_PASSWORD
        self.from_name = settings.EMAIL_FROM_NAME
        self.from_address = settings.EMAIL_FROM_ADDRESS or settings.SMTP_USER
        self.enabled = settings.EMAIL_ENABLED
    
    def send_email(
        self,
        to_email: str,
        subject: str,
        html_body: str,
        text_body: Optional[str] = None
    ) -> bool:
        """
        Envía un email usando SMTP
        
        Args:
            to_email: Dirección de correo del destinatario
            subject: Asunto del correo
            html_body: Cuerpo del mensaje en HTML
            text_body: Cuerpo del mensaje en texto plano (opcional)
            
        Returns:
            True si el email se envió correctamente, False en caso contrario
        """
        
        # Verificar si el envío de emails está habilitado
        if not self.enabled:
            logger.warning("⚠️ Envío de emails deshabilitado en configuración")
            return False
        
        # Validar que el email del destinatario no esté vacío
        if not to_email or not to_email.strip():
            logger.error("❌ No se puede enviar email: dirección de destinatario vacía o inválida")
            return False
        
        # Validar configuración SMTP
        if not self.smtp_user or not self.smtp_password:
            logger.error("❌ Configuración SMTP incompleta (SMTP_USER o SMTP_PASSWORD faltantes)")
            return False
        
        try:
            # Crear mensaje
            message = MIMEMultipart('alternative')
            message['Subject'] = subject
            message['From'] = f"{self.from_name} <{self.from_address}>"
            message['To'] = to_email
            message['Date'] = datetime.now().strftime("%a, %d %b %Y %H:%M:%S %z")
            
            # Agregar cuerpo de texto plano (fallback)
            if text_body:
                part1 = MIMEText(text_body, 'plain', 'utf-8')
                message.attach(part1)
            
            # Agregar cuerpo HTML
            part2 = MIMEText(html_body, 'html', 'utf-8')
            message.attach(part2)
            
            # Conectar al servidor SMTP y enviar
            logger.info(f"📧 Enviando email a {to_email}...")
            with smtplib.SMTP(self.smtp_host, self.smtp_port, timeout=30) as server:
                server.starttls()  # Iniciar conexión TLS
                server.login(self.smtp_user, self.smtp_password)
                server.send_message(message)
            
            logger.info(f"✉️ Email enviado correctamente a {to_email}")
            return True
            
        except smtplib.SMTPAuthenticationError as e:
            logger.error(f"❌ Error de autenticación SMTP. Verifica SMTP_USER y SMTP_PASSWORD")
            logger.error(f"❌ Detalles: {str(e)}")
            return False
            
        except smtplib.SMTPException as e:
            logger.error(f"❌ Error SMTP al enviar email a {to_email}: {str(e)}")
            logger.error(f"❌ Tipo de error: {type(e).__name__}")
            return False
            
        except Exception as e:
            logger.error(f"❌ Error inesperado al enviar email a {to_email}: {str(e)}")
            return False
    
    def enviar_credenciales_chofer(
        self,
        email: str,
        nombre_completo: str,
        usuario: str,
        contrasena: str
    ) -> bool:
        """
        Envía email con credenciales de acceso a un chofer recién registrado
        
        Args:
            email: Email del chofer
            nombre_completo: Nombre completo del chofer
            usuario: Usuario generado
            contrasena: Contraseña temporal
            
        Returns:
            True si el email se envió correctamente
        """
        
        subject = "🔐 Credenciales de Acceso - Sistema Detección Somnolencia"
        
        # Template HTML
        html_body = f"""
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {{
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background-color: #f4f4f4;
                    margin: 0;
                    padding: 0;
                }}
                .container {{
                    max-width: 600px;
                    margin: 40px auto;
                    background-color: #ffffff;
                    border-radius: 10px;
                    overflow: hidden;
                    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                }}
                .header {{
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    color: white;
                    padding: 30px;
                    text-align: center;
                }}
                .header h1 {{
                    margin: 0;
                    font-size: 24px;
                    font-weight: 600;
                }}
                .content {{
                    padding: 40px 30px;
                }}
                .greeting {{
                    font-size: 18px;
                    color: #333;
                    margin-bottom: 20px;
                }}
                .credentials-box {{
                    background-color: #f8f9fa;
                    border-left: 4px solid #667eea;
                    padding: 20px;
                    margin: 25px 0;
                    border-radius: 5px;
                }}
                .credential-item {{
                    margin: 15px 0;
                    display: flex;
                    align-items: center;
                }}
                .credential-label {{
                    font-weight: 600;
                    color: #555;
                    min-width: 140px;
                    display: inline-flex;
                    align-items: center;
                }}
                .credential-value {{
                    color: #333;
                    font-size: 16px;
                    font-family: 'Courier New', monospace;
                    background-color: #fff;
                    padding: 8px 12px;
                    border-radius: 4px;
                    border: 1px solid #ddd;
                }}
                .warning-box {{
                    background-color: #fff3cd;
                    border-left: 4px solid #ffc107;
                    padding: 15px;
                    margin: 25px 0;
                    border-radius: 5px;
                }}
                .warning-box p {{
                    margin: 0;
                    color: #856404;
                    font-size: 14px;
                }}
                .info-box {{
                    background-color: #e7f3ff;
                    border-left: 4px solid #2196F3;
                    padding: 15px;
                    margin: 25px 0;
                    border-radius: 5px;
                }}
                .info-box p {{
                    margin: 0;
                    color: #0c5460;
                    font-size: 14px;
                }}
                .footer {{
                    background-color: #f8f9fa;
                    padding: 20px;
                    text-align: center;
                    color: #6c757d;
                    font-size: 12px;
                    border-top: 1px solid #dee2e6;
                }}
                .icon {{
                    font-size: 20px;
                    margin-right: 8px;
                }}
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>🚗 Sistema de Detección de Somnolencia</h1>
                </div>
                
                <div class="content">
                    <p class="greeting">Hola <strong>{nombre_completo}</strong>,</p>
                    
                    <p style="color: #555; line-height: 1.6;">
                        Tu cuenta ha sido creada exitosamente en el sistema. A continuación encontrarás tus credenciales de acceso:
                    </p>
                    
                    <div class="credentials-box">
                        <div class="credential-item">
                            <span class="credential-label">
                                <span class="icon">👤</span> Usuario:
                            </span>
                            <span class="credential-value">{usuario}</span>
                        </div>
                        <div class="credential-item">
                            <span class="credential-label">
                                <span class="icon">🔑</span> Contraseña:
                            </span>
                            <span class="credential-value">{contrasena}</span>
                        </div>
                    </div>
                    
                    <div class="warning-box">
                        <p>
                            <strong>⚠️ IMPORTANTE:</strong> Por razones de seguridad, te recomendamos cambiar tu contraseña después del primer inicio de sesión. Guarda estas credenciales en un lugar seguro.
                        </p>
                    </div>
                    
                    <div class="info-box">
                        <p>
                            <strong>ℹ️ Nota:</strong> Esta contraseña es temporal y ha sido generada automáticamente. Asegúrate de no compartirla con nadie.
                        </p>
                    </div>
                    
                    <p style="color: #555; margin-top: 30px;">
                        Si tienes alguna pregunta o problema para acceder al sistema, contacta con el administrador.
                    </p>
                    
                    <p style="color: #555; margin-top: 20px;">
                        Saludos cordiales,<br>
                        <strong>Equipo de Administración</strong>
                    </p>
                </div>
                
                <div class="footer">
                    <p>Este es un correo automático, por favor no responder.</p>
                    <p>© 2025 Sistema de Detección de Somnolencia. Todos los derechos reservados.</p>
                </div>
            </div>
        </body>
        </html>
        """
        
        # Texto plano alternativo
        text_body = f"""
        Sistema de Detección de Somnolencia
        
        Hola {nombre_completo},
        
        Tu cuenta ha sido creada exitosamente. Aquí están tus credenciales de acceso:
        
        Usuario: {usuario}
        Contraseña: {contrasena}
        
        ⚠️ IMPORTANTE: Por razones de seguridad, te recomendamos cambiar tu contraseña después del primer inicio de sesión.
        
        Saludos,
        Equipo de Administración
        """
        
        return self.send_email(email, subject, html_body, text_body)
    
    def enviar_asignacion_viaje(
        self,
        email: str,
        nombre_chofer: str,
        origen: str,
        destino: str,
        fecha_programada: str,
        hora_programada: str,
        duracion_estimada: str,
        distancia_km: Optional[float] = None,
        observaciones: Optional[str] = None,
        nombre_empresa: Optional[str] = None
    ) -> bool:
        """
        Envía email con los detalles de un viaje asignado
        
        Args:
            email: Email del chofer
            nombre_chofer: Nombre completo del chofer
            origen: Ciudad de origen
            destino: Ciudad de destino
            fecha_programada: Fecha del viaje (formato DD/MM/YYYY)
            hora_programada: Hora del viaje (formato HH:MM)
            duracion_estimada: Duración estimada del viaje
            distancia_km: Distancia en kilómetros (opcional)
            observaciones: Observaciones adicionales (opcional)
            nombre_empresa: Nombre de la empresa (opcional)
            
        Returns:
            True si el email se envió correctamente
        """
        
        subject = f"🚗 Nueva Ruta Asignada - {fecha_programada}"
        
        # Construir sección de distancia si existe
        distancia_html = ""
        if distancia_km:
            distancia_html = f"""
            <div class="detail-item">
                <span class="detail-icon">📏</span>
                <span class="detail-label">Distancia:</span>
                <span class="detail-value">{distancia_km} km</span>
            </div>
            """
        
        # Construir sección de observaciones si existe
        observaciones_html = ""
        if observaciones:
            observaciones_html = f"""
            <div class="info-box">
                <p><strong>📝 Observaciones:</strong></p>
                <p style="margin-top: 10px;">{observaciones}</p>
            </div>
            """
        
        # Construir sección de empresa si existe
        empresa_html = ""
        if nombre_empresa:
            empresa_html = f"""
            <div class="detail-item">
                <span class="detail-icon">🏢</span>
                <span class="detail-label">Empresa:</span>
                <span class="detail-value">{nombre_empresa}</span>
            </div>
            """
        
        # Template HTML
        html_body = f"""
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {{
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background-color: #f4f4f4;
                    margin: 0;
                    padding: 0;
                }}
                .container {{
                    max-width: 600px;
                    margin: 40px auto;
                    background-color: #ffffff;
                    border-radius: 10px;
                    overflow: hidden;
                    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                }}
                .header {{
                    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
                    color: white;
                    padding: 30px;
                    text-align: center;
                }}
                .header h1 {{
                    margin: 0;
                    font-size: 24px;
                    font-weight: 600;
                }}
                .content {{
                    padding: 40px 30px;
                }}
                .greeting {{
                    font-size: 18px;
                    color: #333;
                    margin-bottom: 20px;
                }}
                .route-box {{
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    color: white;
                    padding: 25px;
                    margin: 25px 0;
                    border-radius: 10px;
                    text-align: center;
                }}
                .route-box h2 {{
                    margin: 0 0 20px 0;
                    font-size: 20px;
                }}
                .route-path {{
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    gap: 15px;
                    font-size: 18px;
                    font-weight: 600;
                }}
                .details-box {{
                    background-color: #f8f9fa;
                    border-radius: 8px;
                    padding: 20px;
                    margin: 25px 0;
                }}
                .detail-item {{
                    display: flex;
                    align-items: center;
                    padding: 12px 0;
                    border-bottom: 1px solid #e9ecef;
                }}
                .detail-item:last-child {{
                    border-bottom: none;
                }}
                .detail-icon {{
                    font-size: 24px;
                    margin-right: 12px;
                    min-width: 30px;
                }}
                .detail-label {{
                    font-weight: 600;
                    color: #555;
                    min-width: 140px;
                }}
                .detail-value {{
                    color: #333;
                    font-size: 15px;
                }}
                .info-box {{
                    background-color: #e7f3ff;
                    border-left: 4px solid #2196F3;
                    padding: 15px;
                    margin: 25px 0;
                    border-radius: 5px;
                }}
                .info-box p {{
                    margin: 0;
                    color: #0c5460;
                    font-size: 14px;
                    line-height: 1.6;
                }}
                .footer {{
                    background-color: #f8f9fa;
                    padding: 20px;
                    text-align: center;
                    color: #6c757d;
                    font-size: 12px;
                    border-top: 1px solid #dee2e6;
                }}
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>🚗 Sistema de Detección de Somnolencia</h1>
                </div>
                
                <div class="content">
                    <p class="greeting">Hola <strong>{nombre_chofer}</strong>,</p>
                    
                    <p style="color: #555; line-height: 1.6;">
                        Se te ha asignado una nueva ruta. A continuación encontrarás los detalles del viaje:
                    </p>
                    
                    <div class="route-box">
                        <h2>📍 Ruta Asignada</h2>
                        <div class="route-path">
                            <span>{origen}</span>
                            <span>→</span>
                            <span>{destino}</span>
                        </div>
                    </div>
                    
                    <div class="details-box">
                        <div class="detail-item">
                            <span class="detail-icon">📅</span>
                            <span class="detail-label">Fecha:</span>
                            <span class="detail-value">{fecha_programada}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-icon">🕐</span>
                            <span class="detail-label">Hora:</span>
                            <span class="detail-value">{hora_programada}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-icon">⏱️</span>
                            <span class="detail-label">Duración estimada:</span>
                            <span class="detail-value">{duracion_estimada}</span>
                        </div>
                        {distancia_html}
                        {empresa_html}
                    </div>
                    
                    {observaciones_html}
                    
                    <p style="color: #555; margin-top: 30px;">
                        Por favor, confirma tu disponibilidad con el administrador si tienes algún inconveniente.
                    </p>
                    
                    <p style="color: #555; margin-top: 20px;">
                        ¡Buen viaje y conduce con seguridad! 🚗💨
                    </p>
                    
                    <p style="color: #555; margin-top: 20px;">
                        Saludos cordiales,<br>
                        <strong>Equipo de Administración</strong>
                    </p>
                </div>
                
                <div class="footer">
                    <p>Este es un correo automático, por favor no responder.</p>
                    <p>© 2025 Sistema de Detección de Somnolencia. Todos los derechos reservados.</p>
                </div>
            </div>
        </body>
        </html>
        """
        
        # Texto plano alternativo
        text_body = f"""
        Sistema de Detección de Somnolencia
        
        Hola {nombre_chofer},
        
        Se te ha asignado una nueva ruta:
        
        📍 Origen: {origen}
        🎯 Destino: {destino}
        📅 Fecha: {fecha_programada}
        🕐 Hora: {hora_programada}
        ⏱️ Duración estimada: {duracion_estimada}
        """
        
        if distancia_km:
            text_body += f"\n📏 Distancia: {distancia_km} km"
        
        if nombre_empresa:
            text_body += f"\n🏢 Empresa: {nombre_empresa}"
        
        if observaciones:
            text_body += f"\n\n📝 Observaciones:\n{observaciones}"
        
        text_body += "\n\nPor favor, confirma tu disponibilidad.\n\nSaludos,\nEquipo de Administración"
        
        return self.send_email(email, subject, html_body, text_body)


# Instancia global del servicio
email_service = EmailService()
