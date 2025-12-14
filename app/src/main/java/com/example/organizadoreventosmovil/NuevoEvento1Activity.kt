import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.organizadoreventosmovil.R

class NuevoEvento1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_evento1)

        // --- 1. OBTENER REFERENCIAS A TODAS LAS VISTAS ---
        val btnVolver = findViewById<Button>(R.id.btnVolver)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarEvento)

        val etNombreEvento = findViewById<EditText>(R.id.editTextNombreEvento)
        val etFechaEvento = findViewById<EditText>(R.id.editTextFechaEvento)
        val etNombreCliente = findViewById<EditText>(R.id.editTextNombreCliente)
        val etTelefono = findViewById<EditText>(R.id.editTextTelefono)
        val etParticipantes = findViewById<EditText>(R.id.editTextParticipantes)
        val etMesas = findViewById<EditText>(R.id.editTextMesas)

        // --- 2. CONFIGURAR EL BOTÓN DE VOLVER (CANCELAR) ---
        btnVolver.setOnClickListener {
            finish() // Cierra esta actividad y vuelve a la anterior
        }

        // --- 3. CONFIGURAR EL BOTÓN DE GUARDAR ---
        btnGuardar.setOnClickListener {
            // Recogemos el texto de cada campo
            val nombreEvento = etNombreEvento.text.toString()
            val fechaEvento = etFechaEvento.text.toString()
            val nombreCliente = etNombreCliente.text.toString()
            val telefono = etTelefono.text.toString()
            val participantes = etParticipantes.text.toString()
            val mesas = etMesas.text.toString()

            // Verificamos que los campos obligatorios no estén vacíos
            if (nombreEvento.isBlank() || fechaEvento.isBlank()) {
                // Si faltan datos, mostramos un mensaje de error
                Toast.makeText(
                    this,
                    "El nombre y la fecha del evento son obligatorios",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Si todo está correcto, creamos el mensaje a mostrar
                val detallesEvento = """
                    Evento Guardado:
                    Nombre: $nombreEvento
                    Fecha: $fechaEvento
                    Cliente: $nombreCliente
                    Teléfono: $telefono
                    Participantes: $participantes
                    Mesas: $mesas
                """.trimIndent()

                // Mostramos un Toast con los detalles
                Toast.makeText(this, detallesEvento, Toast.LENGTH_LONG).show()

                // (Opcional) Después de guardar, podemos cerrar la pantalla
                // finish()
            }
        }
    }
}