package com.example.firebaseworkshop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseworkshop.R
import com.example.firebaseworkshop.model.Contact

class ContactsAdapter(
    private val list: List<Pair<String, Contact>>,
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.nameText)
        val phone: TextView = view.findViewById(R.id.phoneText)
        val email: TextView = view.findViewById(R.id.emailText)
        val deleteBtn: Button = view.findViewById(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val (id, contact) = list[position]

        holder.name.text = contact.name
        holder.phone.text = contact.phone
        holder.email.text = contact.email

        holder.deleteBtn.setOnClickListener {
            onDelete(id)
        }
    }
}