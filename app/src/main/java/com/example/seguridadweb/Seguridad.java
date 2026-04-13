package com.example.seguridadweb;

import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Seguridad {
    // Clave de 16 caracteres para cifrado AES
    private static final String LLAVE = "1234567890123456";

    public static String cifrar(String mensaje) throws Exception {
        SecretKeySpec key = new SecretKeySpec(LLAVE.getBytes(), "AES");
        Cipher cipher = Cipher.Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = cipher.doFinal(mensaje.getBytes());
        return Base64.encodeToString(encVal, Base64.DEFAULT);
    }
}
