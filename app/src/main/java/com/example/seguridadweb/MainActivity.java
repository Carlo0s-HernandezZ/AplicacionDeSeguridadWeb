package com.example.seguridadweb;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // UUID Estándar para Puerto Serie (SPP) - ¡Es la llave universal!
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private EditText messageInput;
    private TextView statusText;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageInput = findViewById(R.id.messageInput);
        statusText = findViewById(R.id.statusText);
        Button connectButton = findViewById(R.id.connectButton);
        Button sendButton = findViewById(R.id.sendButton);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        connectButton.setOnClickListener(v -> iniciarConexion());

        sendButton.setOnClickListener(v -> {
            String msg = messageInput.getText().toString();
            enviarMensajeSeguro(msg);
        });
    }

    private void iniciarConexion() {
        // 1. Verificar Permisos (Crítico en Android 12+)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            // Pedimos los dos de un solo golpe
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                    }, 100);
            return; // Detenemos la ejecución hasta que el usuario acepte
        }

        // 2. Buscar el PC entre los dispositivos ya vinculados
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice pcDevice = null;

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                // TRUCO: Aquí puedes filtrar por nombre si tienes muchos dispositivos
                // Ejemplo: if (device.getName().equals("LAPTOP-T50..."))
                pcDevice = device; // Por ahora tomamos el último o el único
                break;
            }
        }

        if (pcDevice == null) {
            statusText.setText("Error: Vincula el PC en Ajustes Bluetooth primero");
            return;
        }

        // 3. Conectar en segundo plano (NetworkOnMainThreadException prevention)
        final BluetoothDevice deviceFinal = pcDevice;
        new Thread(() -> {
            try {
                // Cancelar búsqueda para acelerar conexión
                bluetoothAdapter.cancelDiscovery();

                socket = deviceFinal.createRfcommSocketToServiceRecord(MY_UUID);
                socket.connect(); // ¡Aquí es donde Windows abre el puerto COM!

                outputStream = socket.getOutputStream();

                // Actualizar UI desde el hilo principal
                new Handler(Looper.getMainLooper()).post(() ->
                        statusText.setText("Conectado a: " + deviceFinal.getName())
                );

            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        statusText.setText("Fallo conexión: " + e.getMessage())
                );
                try { socket.close(); } catch (IOException closeException) { }
            }
        }).start();
    }

    private void enviarMensajeSeguro(String mensaje) {
        if (outputStream == null) {
            Toast.makeText(this, "No estás conectado", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Usamos tu clase de Seguridad blindada
            String cifrado = Seguridad.cifrar(mensaje, this);

            // Agregamos un salto de línea para que la terminal en PC sepa dónde termina
            String paqueteFinal = cifrado + "\n";

            outputStream.write(paqueteFinal.getBytes());
            statusText.setText("Enviado datos cifrados...");
            messageInput.setText("");

        } catch (IOException e) {
            statusText.setText("Error al enviar");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try { if (socket != null) socket.close(); } catch (IOException e) {}
    }
}
