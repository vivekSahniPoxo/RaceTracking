package com.example.racetracking.bpet.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.racetracking.bpet.clickklistnerinterface.OnItemClickListener
import com.example.racetracking.bpet.datamodel.EventModel
import com.example.racetracking.databinding.EventsLayoutBinding


class BPETAdapter(private val mList: MutableList<EventModel.EventModelItem>,private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<BPETAdapter.ViewHOlder>() {
   var onItemClick:((EventModel.EventModelItem)->Unit)?=null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHOlder {
        val itemBinding = EventsLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHOlder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHOlder, position: Int) {
        val items = mList[position]
        holder.bind(items)



    }



    override fun getItemCount(): Int = mList.size

    inner class ViewHOlder(private val itemBinding: EventsLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(items: EventModel.EventModelItem) {
             itemBinding.tvBpet.text = items.eventName
            itemView.setOnClickListener {
                itemClickListener.onItemClick(items)
            }
        }
    }
    }
