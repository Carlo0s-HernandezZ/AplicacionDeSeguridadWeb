package com.example.seguridadweb;

import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Seguridad {
    // La llave DEBE ser exactamente de 16 caracteres para AES-128
    private static final String LLAVE = "EstaEsUnaLlave12";

    public static String cifrar(String mensaje, android.content.Context context) {
        try {
            // 1. Crear la llave secreta en bytes
            SecretKeySpec keySpec = new SecretKeySpec(LLAVE.getBytes("UTF-8"), "AES");

            // 2. Usar AES con Padding (relleno) para que acepte cualquier longitud de texto
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            // 3. Cifrar
            byte[] bytesCifrados = cipher.doFinal(mensaje.getBytes("UTF-8"));

            // 4. Convertir a Base64 para enviar por Bluetooth
            return Base64.encodeToString(bytesCifrados, Base64.NO_WRAP);

        } catch (Exception e) {
            // Esto nos dirá en el Logcat qué está fallando realmente
            e.printStackTrace();
            return "ERRROR: " + e.getMessage();
        }
    }
}
