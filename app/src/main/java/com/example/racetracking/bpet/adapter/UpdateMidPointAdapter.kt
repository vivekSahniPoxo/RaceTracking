package com.example.racetracking.bpet.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.racetracking.bpet.datamodel.UpdateMidPoint
import com.example.racetracking.databinding.RfidLayoutBinding
import com.example.racetracking.databinding.TwoPointMeterLayoutBinding


class UpdateMidPointAdapter( private val mList: ArrayList<UpdateMidPoint>) : RecyclerView.Adapter<UpdateMidPointAdapter.ViewHOlder>() {


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

        fun bind(items: UpdateMidPoint) {
           // itemBinding.srNo.text = items.size.toString()
            itemBinding.tvRfid.text = items.rfid
            itemBinding.tvTime.text = items.time

        }
    }

    fun getPositionOfItem(itemId: String): Int {
        for (index in 0 until mList.size) {
            if (mList[index].rfid == itemId) {
                return index
            }
        }
        return RecyclerView.NO_POSITION // Item not found
    }
    fun clearData() {
        mList.clear()
        notifyDataSetChanged() // Notify the adapter that data has changed
    }
}