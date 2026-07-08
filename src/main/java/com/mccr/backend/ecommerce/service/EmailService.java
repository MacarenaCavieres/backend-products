package com.mccr.backend.ecommerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${frontend.url}")
    private String frontendUrl;

    private final JavaMailSender mailSender;

    /*
     * Tests:
     * Debe crear un mensaje de correo.
     * Debe configurar correctamente el remitente.
     * Debe configurar correctamente el destinatario.
     * Debe configurar correctamente el asunto del correo.
     * Debe construir la URL de recuperación con el token recibido.
     * Debe generar el contenido HTML del correo.
     * Debe enviar el correo mediante JavaMailSender.
     * Debe manejar correctamente cualquier excepción producida durante el envío del
     * correo.
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        String defaultFromEmail = "noreply@ecommerce.com";
        try {

            helper.setFrom(defaultFromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Ecommerce Products");

            String resetUrl = frontendUrl + "/auth/reset-password?token=" + token;

            String htmlContent = buildResetPasswordEmailHtml(resetUrl);

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception ex) {
            System.out.println("Error al tratar de mandar el email a " + toEmail);
            ex.printStackTrace();
        }

    }

    private String buildResetPasswordEmailHtml(String resetUrl) {
        int year = java.time.Year.now().getValue();
        return """
                <html>
                    <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0;">
                        <br/>
                        <br/>
                        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); margin: auto">
                            <tr>
                                <td style="padding: 40px;">
                                    <h2 style="color: #333333;">Password Reset Requested</h2>
                                    <p style="color: #666666; line-height: 1.6;">
                                        Hola,
                                    </p>
                                    <p style="color: #666666; line-height: 1.6;">
                                        Hemos recibido una solicitud para restablecer la contraseña de tu cuenta.
                                        Haz clic en el botón de abajo para cambiar tu contraseña:
                                    </p>

                                    <table role="presentation" border="0" cellpadding="0" cellspacing="0" style="margin: 25px 0;">
                                        <tr>
                                            <td align="center" style="border-radius: 5px;" bgcolor="#0d1b30">
                                                <a href="%s" target="_blank" style="font-size: 16px; font-weight: bold; text-decoration: none; color: #ffffff; padding: 12px 24px; border-radius: 5px; display: inline-block; background-color: #0d1b30;">
                                                    Resetear Contraseña
                                                </a>
                                            </td>
                                        </tr>
                                    </table>

                                    <p style="color: #666666; line-height: 1.6;">
                                        Este enlace es válido durante 15 minutos. Si no solicitó este cambio, ignore este correo electrónico.
                                    </p>
                                    <p style="color: #666666; line-height: 1.6;">
                                        Saludos,<br>
                                        El equipo de Ecommerce Products
                                    </p>
                                </td>
                            </tr>
                        </table>

                        <table role="presentation" border="0" cellpadding="0" cellspacing="0" width="100%%">
                            <tr>
                                <td align="center" style="padding: 20px 0; font-size: 12px; color: #aaaaaa;">
                                    &copy; %d Ecommerce Products. All rights reserved.
                                </td>
                            </tr>
                        </table>
                    </body>
                    </html>
                """
                .formatted(resetUrl, year);
    }

}
