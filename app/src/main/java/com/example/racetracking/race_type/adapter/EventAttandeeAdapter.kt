package com.example.racetracking.race_type.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.racetracking.R
import com.example.racetracking.bpet.clickklistnerinterface.OnItemClickListener
import com.example.racetracking.bpet.clickklistnerinterface.onItemPositionListenr
import com.example.racetracking.bpet.datamodel.EventModel
import com.example.racetracking.databinding.AttandeeDetailsBinding
import com.example.racetracking.databinding.EventsLayoutBinding
import com.example.racetracking.databinding.RaceTypeLayoutTempBinding
import com.example.racetracking.race_type.data.RaceRegsModel
import com.example.racetracking.utils.ClickListener


class EventAttandeeAdapter(private val mList: MutableList<RaceRegsModel.RaceRegsModelItem>,private val itemClickListener: onItemPositionListenr) :
    RecyclerView.Adapter<EventAttandeeAdapter.ViewHOlder>() {



     var getRfidFromActivity = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHOlder {
        val itemBinding = AttandeeDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHOlder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHOlder, position: Int) {
        val items = mList[position]
        holder.bind(items)
    }



    override fun getItemCount(): Int = mList.size

    inner class ViewHOlder(private val itemBinding: AttandeeDetailsBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        @SuppressLint("SuspiciousIndentation")
        fun bind(items: RaceRegsModel.RaceRegsModelItem) {
             itemBinding.tvName.text = items.name
            itemBinding.tvGender.text = items.gender
            itemBinding.tvDob.text = items.dob
            itemBinding.armyNumber.text = items.armyNumber
            itemBinding.unitValue.text = items.unitValue
          //  itemBinding.companyValue.text = items.companyvalue
            itemBinding.startTime.text = items.startTime
            itemBinding.soldireType.text = items.soldierType
            itemBinding.tvRank.text = items.rankValue
            itemBinding.tvCompany.text = items.companyvalue
            itemBinding.tvPosting.text = items.posting
            itemBinding.tvChestNo.text = items.chestNumber


//            if (position % 2 == 0) {
//                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorOdd))
//               itemBinding.tvName.setTextColor(Color.parseColor("#FFFFFF"))
//                itemBinding.tvGender.setTextColor(Color.parseColor("#FFFFFF"))
//                itemBinding.tvDob.setTextColor(Color.parseColor("#FFFFFF"))
//                itemBinding.armyNumber.setTextColor(Color.parseColor("#FFFFFF"))
//                itemBinding.unitValue.setTextColor(Color.parseColor("#FFFFFF"))
//                itemBinding.companyValue.setTextColor(Color.parseColor("#FFFFFF"))
//                itemBinding.startTime.setTextColor(Color.parseColor("#FFFFFF"))
//                itemBinding.soldireType.setTextColor(Color.parseColor("#FFFFFF"))
//                itemBinding.tvRank.setTextColor(Color.parseColor("#FFFFFF"))
//                itemBinding.tvCompany.setTextColor(Color.parseColor("#FFFFFF"))
//                itemBinding.tvPosting.setTextColor(Color.parseColor("#FFFFFF"))
//                itemBinding.tvChestNo.setTextColor(Color.parseColor("#FFFFFF"))
//            } else {
//                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorEven))
//                itemBinding.tvGender.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorEven))
//                itemBinding.tvDob.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorEven))
//                itemBinding.armyNumber.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorEven))
//                itemBinding.unitValue.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorEven))
//                itemBinding.companyValue.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorEven))
//                itemBinding.startTime.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorEven))
//                itemBinding.soldireType.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorEven))
//                itemBinding.tvRank.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorEven))
//                itemBinding.tvCompany.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorEven))
//                itemBinding.tvPosting.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorEven))
//                itemBinding.tvChestNo.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorEven))
       //     }

            itemView.setOnClickListener {
                itemView.setOnClickListener {
                    itemClickListener.onItemClick(items, adapterPosition)
                    Log.d("itemsss",items.name.toString())
                }
            }






        }




    }

    fun clear(){
        mList.clear()
        notifyDataSetChanged()
    }



}