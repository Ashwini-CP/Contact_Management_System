package com.example.firebaseworkshop

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.firebaseworkshop.adapter.ContactsAdapter
import com.example.firebaseworkshop.model.Contact
import com.google.firebase.analytics.FirebaseAnalytics

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ContactsAdapter
    private val contactList = mutableListOf<Pair<String, Contact>>()
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        requestNotificationPermission()

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            Log.d("FCM_TOKEN", it)
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        analytics = FirebaseAnalytics.getInstance(this)

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val phoneInput = findViewById<EditText>(R.id.phoneInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)

        val addBtn = findViewById<Button>(R.id.addBtn)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        adapter = ContactsAdapter(contactList) { docId ->
            db.collection("contacts").document(docId).delete()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ✅ ADD CONTACT
        addBtn.setOnClickListener {

            val name = nameInput.text.toString()
            val phone = phoneInput.text.toString()
            val email = emailInput.text.toString()

            if (name.isNotEmpty() && phone.isNotEmpty()) {

                val contact = Contact(
                    name = name,
                    phone = phone,
                    email = email,
                    userId = auth.currentUser?.uid ?: ""
                )

                db.collection("contacts").add(contact)
                    .addOnSuccessListener {
                        Log.d("DEBUG", "Contact Added")
                        Toast.makeText(this, "Contact Added", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Log.e("DEBUG", "Error: ${it.message}")
                        Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                    }

                nameInput.setText("")
                phoneInput.setText("")
                emailInput.setText("")

                analytics.logEvent("contact_added", null)

            } else {
                Toast.makeText(this, "Enter name & phone", Toast.LENGTH_SHORT).show()
            }
        }

        logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        loadContacts()
    }

    // ✅ LOAD CONTACTS
    private fun loadContacts() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("CONTACT_DEBUG", "No user logged in")
            return
        }

        db.collection("contacts")
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("CONTACT_DEBUG", "Error: ${error.message}")
                    Toast.makeText(this, "Error loading: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                contactList.clear()

                snapshot?.documents?.forEach {
                    val contact = it.toObject(Contact::class.java)
                    if (contact != null) {
                        contactList.add(Pair(it.id, contact))
                    }
                }

                Log.d("CONTACT_DEBUG", "Total Contacts: ${contactList.size}")

                adapter.notifyDataSetChanged()
            }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }
}