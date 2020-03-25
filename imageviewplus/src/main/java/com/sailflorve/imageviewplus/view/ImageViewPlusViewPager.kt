package com.sailflorve.imageviewplus.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.sailflorve.imageviewplus.R

class ImageViewPlusViewPager : RelativeLayout {
    private lateinit var viewPager: ViewPager

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        val view: View = View.inflate(context, R.layout.image_view_plus_view_pager, this)
        viewPager = view.findViewById(R.id.view_pager)
    }

    fun setImageViews(vararg view: ImageViewPlus) {
        setImageViews(view.asList())
    }

    fun setImageViews(list: List<ImageViewPlus>) {
        viewPager.adapter = ViewPagerAdapter(list)
        list.forEach {
            it.setDraggable(false)
        }
    }

    fun getViewPager(): ViewPager {
        return viewPager
    }

    private class ViewPagerAdapter(private val viewList: List<ImageViewPlus>) : PagerAdapter() {

        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

        override fun getCount(): Int = viewList.size

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            container.addView(viewList[position])
            return viewList[position]
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(viewList[position])
        }
    }
}