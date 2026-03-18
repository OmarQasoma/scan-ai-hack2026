package com.scanai.scanai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.scanai.scanai.databinding.ActivityStandarderBinding

class StandarderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStandarderBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStandarderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        showDailyStandards()

        binding.btnDailyStandards.setOnClickListener {
            showDailyStandards()
        }

        binding.btnMachineStandards.setOnClickListener {
            showMachineStandards()
        }

        binding.btnSendDailyReport.setOnClickListener {
            sendDailyReport()
        }

        binding.btnSearchMachine.setOnClickListener {
            val machineNumber = binding.etMachineNumber.text.toString().trim()
            if (machineNumber.isEmpty()) {
                binding.etMachineNumber.error = "Ange maskinnummer"
                binding.etMachineNumber.requestFocus()
            } else {
                loadMachineStandards(machineNumber)
            }
        }

        setupStandardButtons()
    }

    private fun showDailyStandards() {
        binding.layoutDailyStandards.visibility = android.view.View.VISIBLE
        binding.layoutMachineStandards.visibility = android.view.View.GONE

        binding.btnDailyStandards.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#163A70"))
        )
        binding.btnDailyStandards.setTextColor(android.graphics.Color.WHITE)

        binding.btnMachineStandards.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D6DEE8"))
        )
        binding.btnMachineStandards.setTextColor(android.graphics.Color.parseColor("#1F2937"))
    }

    private fun showMachineStandards() {
        binding.layoutDailyStandards.visibility = android.view.View.GONE
        binding.layoutMachineStandards.visibility = android.view.View.VISIBLE

        binding.btnMachineStandards.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#163A70"))
        )
        binding.btnMachineStandards.setTextColor(android.graphics.Color.WHITE)

        binding.btnDailyStandards.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#D6DEE8"))
        )
        binding.btnDailyStandards.setTextColor(android.graphics.Color.parseColor("#1F2937"))
    }

    private fun sendDailyReport() {
        val report = hashMapOf(
            "checklistaQ" to binding.cbChecklistaQ.isChecked,
            "checklistaSHE" to binding.cbChecklistaSHE.isChecked,
            "overlapp" to binding.cbOverlapp.isChecked,
            "safety" to binding.cbSafety.isChecked,
            "cleaning" to binding.cbCleaning.isChecked,
            "comment" to binding.etDailyComment.text.toString().trim(),
            "status" to "Daglig standard klar",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("daily_standard_reports")
            .add(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Daglig rapport skickad ✔", Toast.LENGTH_SHORT).show()
                clearDailyForm()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Fel: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun clearDailyForm() {
        binding.cbChecklistaQ.isChecked = false
        binding.cbChecklistaSHE.isChecked = false
        binding.cbOverlapp.isChecked = false
        binding.cbSafety.isChecked = false
        binding.cbCleaning.isChecked = false
        binding.etDailyComment.setText("")
    }

    private fun loadMachineStandards(machineNumber: String) {

        // 1) Hämta machine info
        db.collection("machines")
            .document(machineNumber)
            .get()
            .addOnSuccessListener { machineDoc ->
                if (machineDoc.exists()) {
                    val byggnad = machineDoc.get("byggnad")?.toString() ?: ""
                    val line = machineDoc.get("line")?.toString() ?: ""
                    val machine = machineDoc.get("machine")?.toString() ?: ""

                    binding.tvMachineInfo.text = "$byggnad / $line / $machine"
                } else {
                    binding.tvMachineInfo.text = "Ingen maskininformation hittades för $machineNumber"
                }
            }
            .addOnFailureListener { e ->
                binding.tvMachineInfo.text = "Fel vid hämtning av maskininformation"
                Toast.makeText(this, "Maskininfo fel: ${e.message}", Toast.LENGTH_LONG).show()
            }

        // 2) Hämta machine standards
        db.collection("machine_standards")
            .document(machineNumber)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.tvStandard1Title.text = doc.get("title1")?.toString() ?: "Skärbyte"
                    binding.tvStandard1Desc.text = doc.get("desc1")?.toString() ?: "Instruktion saknas"

                    binding.tvStandard2Title.text = doc.get("title2")?.toString() ?: "Program byte"
                    binding.tvStandard2Desc.text = doc.get("desc2")?.toString() ?: "Instruktion saknas"

                    binding.tvStandard3Title.text = doc.get("title3")?.toString() ?: "Rigg"
                    binding.tvStandard3Desc.text = doc.get("desc3")?.toString() ?: "Instruktion saknas"

                    binding.btnStandard1Pdf.tag = doc.get("pdf1")?.toString() ?: ""
                    binding.btnStandard1Video.tag = doc.get("video1")?.toString() ?: ""
                    binding.btnStandard1Images.tag = doc.get("images1")?.toString() ?: ""

                    binding.btnStandard2Pdf.tag = doc.get("pdf2")?.toString() ?: ""
                    binding.btnStandard2Video.tag = doc.get("video2")?.toString() ?: ""
                    binding.btnStandard2Images.tag = doc.get("images2")?.toString() ?: ""

                    binding.btnStandard3Pdf.tag = doc.get("pdf3")?.toString() ?: ""
                    binding.btnStandard3Video.tag = doc.get("video3")?.toString() ?: ""
                    binding.btnStandard3Images.tag = doc.get("images3")?.toString() ?: ""
                } else {
                    binding.tvStandard1Title.text = doc.get("title1")?.toString() ?: "Skärbyte"
                    binding.tvStandard1Desc.text = doc.get("desc1")?.toString() ?: "Instruktion saknas"

                    binding.tvStandard2Title.text = doc.get("title2")?.toString() ?: "Program byte"
                    binding.tvStandard2Desc.text = doc.get("desc2")?.toString() ?: "Instruktion saknas"

                    binding.tvStandard3Title.text = doc.get("title3")?.toString() ?: "Rigg"
                    binding.tvStandard3Desc.text = doc.get("desc3")?.toString() ?: "Instruktion saknas"

                    binding.tvStandard4Title.text = doc.get("title4")?.toString() ?: "Justera"
                    binding.tvStandard4Desc.text = doc.get("desc4")?.toString() ?: "Instruktion saknas"

                    binding.btnStandard4Pdf.tag = doc.get("pdf4")?.toString() ?: ""
                    binding.btnStandard4Video.tag = doc.get("video4")?.toString() ?: ""
                    binding.btnStandard4Images.tag = doc.get("images4")?.toString() ?: ""

                    Toast.makeText(this, "Ingen maskinstandard hittades för $machineNumber", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Fel vid hämtning av standarder: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    private fun setupStandardButtons() {
        binding.btnStandard1Pdf.setOnClickListener { openLink(it.tag?.toString()) }
        binding.btnStandard1Video.setOnClickListener { openLink(it.tag?.toString()) }
        binding.btnStandard1Images.setOnClickListener { openLink(it.tag?.toString()) }

        binding.btnStandard2Pdf.setOnClickListener { openLink(it.tag?.toString()) }
        binding.btnStandard2Video.setOnClickListener { openLink(it.tag?.toString()) }
        binding.btnStandard2Images.setOnClickListener { openLink(it.tag?.toString()) }

        binding.btnStandard3Pdf.setOnClickListener { openLink(it.tag?.toString()) }
        binding.btnStandard3Video.setOnClickListener { openLink(it.tag?.toString()) }
        binding.btnStandard3Images.setOnClickListener { openLink(it.tag?.toString()) }
    }

    private fun openLink(url: String?) {
        if (url.isNullOrEmpty()) {
            Toast.makeText(this, "Ingen länk tillgänglig ännu", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}