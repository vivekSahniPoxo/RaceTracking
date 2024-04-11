package com.example.racetracking.reports.ppt_report

import android.annotation.SuppressLint
import android.graphics.Color
import com.example.racetracking.reports.ReportDataModel





import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible

import androidx.recyclerview.widget.RecyclerView
import com.example.racetracking.R
import com.example.racetracking.databinding.PptReportLayoutBinding

import com.example.racetracking.databinding.ReportLayoutBinding
import com.example.racetracking.reports.pptReportDataModel


class PPTReportAdapter(private var mList: MutableList<pptReportDataModel>) : RecyclerView.Adapter<PPTReportAdapter.ViewHolder>() {

    var eventNameFromActivity = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = PptReportLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = mList.size

    inner class ViewHolder(private val itemBinding: PptReportLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(item: pptReportDataModel) {
            itemBinding.tvChestNo.text = item.chestNumber
            itemBinding.tvName.text = item.name
            itemBinding.tvArmyNumber.text = item.armyNo
            itemBinding.tvRank.text = item.rank
            itemBinding.tvStatus.text = item.status
            itemBinding.tv5mtrShuttle.text = item.EventName
            itemBinding.tvSoldierType.text = item.solidierType
            itemBinding.tvGender.text =  item.gender
            itemBinding.tvAge.text = item.age.toString()
            itemBinding.tvPosting.text  =  item.posting
            itemBinding.tvCompany.text = item.company

            if (item.EventName == eventNameFromActivity){
                itemBinding.constraint.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.green2))
                Toast.makeText(itemView.context,"List Updated",Toast.LENGTH_SHORT).show()
            } else if (item.EventName !=eventNameFromActivity && eventNameFromActivity.isNotEmpty()) {
                itemBinding.constraint.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.red))
               // Toast.makeText(itemView.context, "$eventNameFromActivity No Found ", Toast.LENGTH_SHORT).show()
            }
            else{
                itemBinding.constraint.setBackgroundColor(Color.TRANSPARENT)
            }

        }


    }






    @SuppressLint("NotifyDataSetChanged")
    fun updateList(updatedList: List<pptReportDataModel>,loader: ProgressBar) {
        //val filteredList = updatedList.distinctBy { it.status }
        loader.isVisible = true
        mList.clear()
        mList.addAll(updatedList)
        notifyDataSetChanged()
        loader.isVisible = false
    }

    fun clear(){
        mList.clear()
        notifyDataSetChanged()
    }
}
