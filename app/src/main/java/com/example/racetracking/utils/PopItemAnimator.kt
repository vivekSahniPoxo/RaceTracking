package com.example.racetracking.utils

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class PopItemAnimator : DefaultItemAnimator() {
    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        // Customize the "pop" animation here
        return super.animateRemove(holder)
    }
}
