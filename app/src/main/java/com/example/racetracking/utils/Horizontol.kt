package com.example.racetracking.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView
import androidx.recyclerview.widget.RecyclerView

class HorizontalRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private var horizontalScrollView: HorizontalScrollView? = null

    fun setHorizontalScrollView(horizontalScrollView: HorizontalScrollView) {
        this.horizontalScrollView = horizontalScrollView
    }

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        horizontalScrollView?.scrollBy(dx, 0)
    }
}
