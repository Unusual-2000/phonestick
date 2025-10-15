package streetwalrus.usbmountr

import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EncoderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encoder)

        title = getString(R.string.title_activity_encoder)

        val inputText = findViewById<EditText>(R.id.inputText)
        val encodeButton = findViewById<Button>(R.id.encodeButton)
        val decodeButton = findViewById<Button>(R.id.decodeButton)
        val resultText = findViewById<TextView>(R.id.resultText)

        encodeButton.setOnClickListener {
            val text = inputText.text.toString()
            if (text.isNotEmpty()) {
                val encodedBytes = Base64.encode(text.toByteArray(), Base64.DEFAULT)
                resultText.text = String(encodedBytes)
            }
        }

        decodeButton.setOnClickListener {
            val text = inputText.text.toString()
            if (text.isNotEmpty()) {
                try {
                    val decodedBytes = Base64.decode(text, Base64.DEFAULT)
                    resultText.text = String(decodedBytes)
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(this, getString(R.string.encoder_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
