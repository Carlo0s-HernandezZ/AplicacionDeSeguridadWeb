package com.example.seguridadweb;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private EditText messageInput;
    private TextView statusText;
    private OutputStream outputStream; // Asegúrate de inicializar esto en tu conexión BT

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageInput = findViewById(R.id.messageInput);
        statusText = findViewById(R.id.statusText);
        Button sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(messageInput.getText().toString());
            }
        });
    }

    private void sendMessage(String message) {
        if (message.isEmpty()) {
            Toast.makeText(this, "Escribe algo primero", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // CORRECCIÓN: Pasamos el mensaje y el contexto (this)
            String mensajeCifrado = Seguridad.cifrar(message, this);

            if (outputStream != null) {
                outputStream.write(mensajeCifrado.getBytes());
                outputStream.flush();
                statusText.setText("Enviado (Cifrado): " + mensajeCifrado);
                messageInput.setText(""); // Limpiamos por seguridad
            } else {
                statusText.setText("Error: No hay conexión");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
