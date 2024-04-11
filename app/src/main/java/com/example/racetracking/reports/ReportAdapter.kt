package com.example.racetracking.reports



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
import com.example.racetracking.databinding.ReportLayoutBinding
import com.example.racetracking.race_type.data.RaceRegsModel
import com.example.racetracking.utils.ClickListener


class ReportAdapter(private val mList: MutableList<ReportDataModel>) : RecyclerView.Adapter<ReportAdapter.ViewHolder>() {


    private val uniqueChestNumbers = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ReportLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = mList.size

    inner class ViewHolder(private val itemBinding: ReportLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(item: ReportDataModel) {
            itemBinding.tvChestNo.text = item.chestNumber
            itemBinding.tvFiveKmTruningPoint.text = item.FiveKMTurning
            itemBinding.tvTwoPointFiveMtr.text = item.twoPointMtrDitch
            itemBinding.tvVRope.text = item.vRope
            itemBinding.tvHRope.text = item.HRope
            itemBinding.tvName.text = item.name
            itemBinding.tvArmyNumber.text = item.armyNo
            itemBinding.tvRank.text = item.rank
            itemBinding.tvSoldierType.text = item.solidierType
            itemBinding.tvGender.text =  item.gender
            itemBinding.tvAge.text = item.age.toString()
            itemBinding.tvPosting.text  =  item.posting
            itemBinding.tvCompany.text = item.company

        }
    }


    fun updateList(updatedList: List<ReportDataModel>) {
        // Clear the existing set of unique chest numbers
        uniqueChestNumbers.clear()

        // Filter the list and add only items with unique chest numbers to the set
        val filteredList = mutableListOf<ReportDataModel>()
        for (item in updatedList) {
            if (uniqueChestNumbers.add(item.chestNumber)) {
                filteredList.add(item)
            }
        }

        // Clear the existing list and add the filtered items
        mList.clear()
        mList.addAll(filteredList)

        // Notify the adapter that the data has changed
        notifyDataSetChanged()
    }





//    fun updateList(updatedList: List<ReportDataModel>,) {
//        val filteredList = updatedList.distinctBy { it.chestNumber }
//        mList.clear()
//        mList.addAll(filteredList)
//        notifyDataSetChanged()
//    }
}
