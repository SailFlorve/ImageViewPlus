package com.sailflorve.imageviewplus.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sailflorve.imageviewplus.view.ImageViewPlus
import kotlinx.android.synthetic.main.activity_image_view_pager.*

class ImageViewPagerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view_pager)

        val list: List<ImageViewPlus> = listOf(
                ImageViewPlus(this).apply {
                    setImage(R.drawable.bg_example1)
                },

                ImageViewPlus(this).apply {
                    setImage(R.drawable.timg)
                }
        )

        viewPager.setImageViews(list)
    }

}
