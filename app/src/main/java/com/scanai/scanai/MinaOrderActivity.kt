package com.scanai.scanai

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.scanai.scanai.databinding.ActivityMinaOrderBinding

class MinaOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMinaOrderBinding
    private val db = FirebaseFirestore.getInstance()
    private val orderList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMinaOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        loadOrders()
    }

    private fun loadOrders() {
        db.collection("orders")
            .get()
            .addOnSuccessListener { result ->
                orderList.clear()

                for (document in result) {
                    val svNumber = document.get("svNumber")?.toString() ?: ""
                    val machine = document.get("machine")?.toString() ?: ""
                    val orderType = document.get("orderType")?.toString() ?: ""
                    val role = document.get("role")?.toString() ?: ""
                    val description = document.get("description")?.toString() ?: ""
                    val stopped = document.get("stopped")?.toString() ?: ""
                    val status = document.get("status")?.toString() ?: "Okänd status"

                    val item = """
                        Status: $status
                        Nummer: $svNumber
                        Maskin: $machine
                        Typ: $orderType
                        Roll: $role
                        Stopp: $stopped
                        Problem: $description
                    """.trimIndent()

                    orderList.add(item)
                }

                if (orderList.isEmpty()) {
                    orderList.add("Inga orders hittades")
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    orderList
                )
                binding.listViewOrders.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Kunde inte hämta orders: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}