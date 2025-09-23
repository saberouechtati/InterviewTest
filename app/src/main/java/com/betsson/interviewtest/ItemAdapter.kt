package com.betsson.interviewtest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(var bets: List<Bet>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false) as View
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return bets.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = bets[position]

        holder.view.findViewById<TextView>(R.id.name).text = item.type
        holder.view.findViewById<TextView>(R.id.sellIn).text = item.sellIn.toString()
        //holder.view.findViewById<ImageView>(R.id.imageView).drawable =
    }

}
