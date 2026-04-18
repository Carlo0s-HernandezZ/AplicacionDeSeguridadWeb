package com.example.seguridadweb;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class Seguridad {

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String ALIAS = "mi_llave_segura";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    public static String cifrar(String mensaje, Context context) {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);

            // Generar llave en hardware si no existe
            if (!keyStore.containsAlias(ALIAS)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);

                keyGenerator.init(new KeyGenParameterSpec.Builder(ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .build());
                keyGenerator.generateKey();
            }

            // Proceso de cifrado
            SecretKey key = (SecretKey) keyStore.getKey(ALIAS, null);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] iv = cipher.getIV(); // Vector de inicialización
            byte[] encryption = cipher.doFinal(mensaje.getBytes("UTF-8"));

            // Combinamos IV + Datos cifrados para que el receptor pueda descifrar
            byte[] combined = new byte[iv.length + encryption.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryption, 0, combined, iv.length, encryption.length);

            return Base64.encodeToString(combined, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR_CIFRADO";
        }
    }
}
