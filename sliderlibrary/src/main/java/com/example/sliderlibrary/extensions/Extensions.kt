package com.example.sliderlibrary

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.target.Target
import java.lang.Exception


/**
 * Created by Jemo on 12/5/16.
 */
fun ImageView.loadImage(imgUrl: String?, progressBar: ProgressBar){
    Glide.with(context).load(imgUrl)
            .listener(object : RequestListener<String, GlideDrawable> {
                override fun onException(e: Exception?, model: String?, target: com.bumptech.glide.request.target.Target<GlideDrawable>?, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: GlideDrawable?, model: String?, target: Target<GlideDrawable>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                    progressBar.visibility = View.GONE
                    return false
                }
            })
            .into(this)
}

fun ImageView.loadImage(imgDrawable: Drawable?){
    this.setImageDrawable(imgDrawable)
}

fun View.stayVisibleOrGone(stay: Boolean){
    this.visibility = if (stay) View.VISIBLE else View.GONE
}