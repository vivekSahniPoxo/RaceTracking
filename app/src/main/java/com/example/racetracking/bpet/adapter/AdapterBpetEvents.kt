package com.example.racetracking.bpet.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.racetracking.bpet.datamodel.Rfid



import android.graphics.Color
import android.view.LayoutInflater

import androidx.core.content.ContextCompat

import com.example.racetracking.R

import com.example.racetracking.databinding.TwoPointMeterLayoutBinding


class AdapterBpetEvents(private val mList: ArrayList<Rfid>) : RecyclerView.Adapter<AdapterBpetEvents.ViewHOlder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHOlder {
        val itemBinding = TwoPointMeterLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHOlder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHOlder, position: Int) {
        val items = mList[position]
        holder.bind(items)
    }

    override fun getItemCount(): Int = mList.size

    class ViewHOlder(private val itemBinding: TwoPointMeterLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(items: Rfid) {
            // itemBinding.srNo.text = items.srNo.toString()
            itemBinding.tvRfid.text = items.rfid
//            when (items.getIsPassed()) {
//                "1" -> {
//                    itemBinding.root.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.green2))
//
//                }
//                "2" -> {
//                    itemBinding.root.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.red))
//                }
//                else -> {
//                    itemBinding.root.setBackgroundColor(Color.TRANSPARENT)
//                }
//
//            }
        }
    }

    fun clearData() {
        mList.clear()
        notifyDataSetChanged() // Notify the adapter that data has changed
    }

    fun updateIsPassed(itemId: String, newValue: String) {
        val itemToUpdate = mList.find { it.rfid == itemId }
        itemToUpdate?.setIsPassed(newValue)
        itemToUpdate?.let {
            val position = mList.indexOf(it)
            if (position != -1) {
                notifyItemChanged(position)
            }
        }
    }

    fun updateAllStatus(newStatus: String) {
        mList.forEach { item ->
            item.setIsPassed(newStatus)
        }
        notifyDataSetChanged()
    }

    fun getPositionOfItem(itemId: String): Int {
        for (index in 0 until mList.size) {
            if (mList[index].rfid == itemId) {
                return index
            }
        }
        return RecyclerView.NO_POSITION // Item not found
    }

}