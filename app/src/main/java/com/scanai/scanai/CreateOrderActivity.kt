package com.scanai.scanai

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import com.scanai.scanai.databinding.ActivityCreateOrderBinding

class CreateOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateOrderBinding
    private val db = FirebaseFirestore.getInstance()

    private val qrScannerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val intentResult = IntentIntegrator.parseActivityResult(
                result.resultCode,
                result.data
            )

            if (intentResult != null && intentResult.contents != null) {
                val scanned = intentResult.contents.trim()
                val onlyNumber = scanned.removePrefix("SV")
                binding.etSvNumber.setText(onlyNumber)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnScan.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setPrompt("Scan machine QR")
            integrator.setBeepEnabled(true)
            integrator.setOrientationLocked(true)
            qrScannerLauncher.launch(integrator.createScanIntent())
        }

        binding.etSvNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val number = s.toString().trim()

                if (number.length >= 6) {
                    fetchMachineData(number)
                } else {
                    clearMachineFields()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnSend.setOnClickListener {
            sendOrder()
        }
    }

    private fun fetchMachineData(number: String) {
        db.collection("machines")
            .document(number)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.etByggnad.setText(doc.getString("byggnad") ?: "")
                    binding.etLine.setText(doc.getString("line") ?: "")
                    binding.etMachine.setText(doc.getString("machine") ?: "")
                } else {
                    clearMachineFields()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Kunde inte hämta maskindata", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendOrder() {
        val svNumber = binding.etSvNumber.text.toString().trim()
        val byggnad = binding.etByggnad.text.toString().trim()
        val line = binding.etLine.text.toString().trim()
        val machine = binding.etMachine.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (svNumber.isEmpty()) {
            binding.etSvNumber.error = "Ange nummer"
            binding.etSvNumber.requestFocus()
            return
        }

        if (description.isEmpty()) {
            binding.etDescription.error = "Beskriv problemet"
            binding.etDescription.requestFocus()
            return
        }

        val orderType = when (binding.rgOrderType.checkedRadioButtonId) {
            R.id.rbAkut -> "Akut"
            R.id.rbPlanerad -> "Planerad"
            else -> ""
        }

        val role = when (binding.rgRole.checkedRadioButtonId) {
            R.id.rbElektriker -> "Elektriker"
            R.id.rbMekaniker -> "Mekaniker"
            else -> ""
        }

        val stopped = when (binding.rgStopped.checkedRadioButtonId) {
            R.id.rbYes -> "Ja"
            R.id.rbNo -> "Nej"
            else -> ""
        }

        val order = hashMapOf(
            "svNumber" to svNumber,
            "byggnad" to byggnad,
            "line" to line,
            "machine" to machine,
            "description" to description,
            "orderType" to orderType,
            "role" to role,
            "stopped" to stopped,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("orders")
            .add(order)
            .addOnSuccessListener {
                Toast.makeText(this, "Order skickad ✔", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Fel vid skick av order", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearMachineFields() {
        binding.etByggnad.setText("")
        binding.etLine.setText("")
        binding.etMachine.setText("")
    }
}