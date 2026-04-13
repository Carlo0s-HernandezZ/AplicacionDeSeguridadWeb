package com.example.seguridadweb;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // Variables de Bluetooth (Capítulo 5 y 6)
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;

    // UI Elements
    private TextView statusText;
    private ProgressBar progressBar;

    // Configuración (Capítulo 5.4.2)
    // CAMBIA ESTA DIRECCIÓN POR LA DE TU PC (Se obtiene en Configuración de Bluetooth en Windows)
    private static final String DEVICE_ADDRESS = "00:11:22:33:44:55";
    private static final UUID UUID_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Configuración de márgenes de sistema (EdgeToEdge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar componentes
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        statusText = findViewById(R.id.statusText);
        progressBar = findViewById(R.id.progressBar);
        Button connectButton = findViewById(R.id.connectButton);
        Button sendButton = findViewById(R.id.sendButton);
        EditText messageInput = findViewById(R.id.messageInput);

        // Evento para conectar (Capítulo 5.4.2)
        connectButton.setOnClickListener(v -> connectToBluetoothDevice());

        // Evento para enviar datos (Capítulo 6.3.2)
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty()) {
                sendMessage(message);
                messageInput.setText(""); // Limpiar campo
            }
        });
    }

    private void connectToBluetoothDevice() {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
        try {
            // Crear conexión (Capítulo 5.4.2)
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_SERIAL);
            bluetoothSocket.connect();

            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

            statusText.setText("Conectado a: " + DEVICE_ADDRESS);
            startListening(); // Iniciar hilo de escucha (Capítulo 6.4)

        } catch (IOException e) {
            e.printStackTrace();
            statusText.setText("Error: No se pudo conectar");
        }
    }

    private void sendMessage(String message) {
        if (outputStream != null) {
            try {
                // CIFRAMOS EL MENSAJE AQUÍ (Capítulo 8)
                String mensajeCifrado = Seguridad.cifrar(message);

                outputStream.write(mensajeCifrado.getBytes());
                outputStream.flush();
                statusText.setText("Enviado (Cifrado): " + mensajeCifrado);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void startListening() {
        // Hilo secundario para no bloquear la pantalla (Capítulo 6.4)
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    final String receivedMessage = new String(buffer, 0, bytes);

                    // Actualizar UI desde el hilo secundario
                    runOnUiThread(() -> statusText.setText("Servidor dice: " + receivedMessage));

                } catch (IOException e) {
                    break; // Conexión perdida
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cierre adecuado (Capítulo 7.4)
        try {
            if (bluetoothSocket != null) bluetoothSocket.close();
            if (outputStream != null) outputStream.close();
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
