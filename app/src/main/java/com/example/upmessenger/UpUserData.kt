package com.example.upmessenger

import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

data class UpUserData(

    val name: String ="UpUser",
    val profilePic: String,
    var selected: Int=0,
    val userId: String,
    val status: String = "UpMessenger User"
) {
    constructor(): this("","",0,"","")

    @BindingAdapter("loadProfileImage")
    fun loadImage(imageView: ImageView, imageUrl: String) {
        Glide.with(imageView.context)
            .load(imageUrl)
            .apply(RequestOptions.placeholderOf(R.drawable.ic_launcher_foreground))
            .into(imageView)
    }


    companion object {
        @BindingAdapter("selectedVisibility")
        @JvmStatic
        fun selectedVisibility(imageView: ImageView, isSelected: Int) {
            Log.d("USER SELECTED",isSelected.toString())
            if (isSelected==1)
                imageView.visibility = View.VISIBLE
            else {
                imageView.visibility = View.GONE
            }

        }
    }

}
