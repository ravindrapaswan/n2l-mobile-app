package practice.english.n2l.bindingadapters

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import practice.english.n2l.R

class ImageViewBindingAdapters {
    companion object {
        @BindingAdapter("app:isVisible")
        @JvmStatic
        fun setVisibility(imageView: ImageView, isVisible: String,filePath:String?) {
            if(imageView.id== R.id.ImgUpload)
                imageView.visibility = if (isVisible == "N" && filePath==null) View.VISIBLE else View.INVISIBLE
        }

        @BindingAdapter("app:isVisible")
        fun setVisibility(imageView: ImageView, isVisible: Boolean) {
            imageView.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        }

        @BindingAdapter("app:textViewVisible")
        @JvmStatic
        fun textViewVisible(imageView: ImageView, practiceLocalFilePath: String) {
            if(practiceLocalFilePath.isEmpty())
                imageView.visibility=View.VISIBLE
            else
                imageView.visibility=View.VISIBLE
        }
        @BindingAdapter("app:imagePath")
        @JvmStatic
        fun loadImage(imageView: ImageView, imagePath: String?) {
            if (!imagePath.isNullOrEmpty()) {
                imageView.setImageURI(Uri.parse(imagePath))
            }
        }
    }
}