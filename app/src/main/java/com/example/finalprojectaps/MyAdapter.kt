package com.example.finalprojectaps

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private var myDataSet: Array<String>, private var hint: String = "") :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hintTextView: TextView = itemView.findViewById(R.id.hint_textview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.hintTextView.text = hint
    }

    override fun getItemCount() = myDataSet.size

    fun updateHint(newHint: String) {
        hint = newHint
        notifyDataSetChanged()
    }

    fun updateData(newData: Array<String>, hint: String) {
        myDataSet = newData
        notifyDataSetChanged()
    }
}
