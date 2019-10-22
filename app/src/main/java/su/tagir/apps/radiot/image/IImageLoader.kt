package su.tagir.apps.radiot.image

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import java.io.File

interface IImageLoader {
    fun display(@DrawableRes res: Int, imageView: ImageView, config: ImageConfig? = null)
    fun display(url: String, imageView: ImageView, config: ImageConfig? = null)
    fun display(file: File, imageView: ImageView, config: ImageConfig? = null)
    fun display(uri: Uri, imageView: ImageView, config: ImageConfig? = null)
    fun load(url: String, target: Target, config: ImageConfig? = null)
    fun load(file: File, target: Target, config: ImageConfig? = null)
    fun load(uri: Uri, target: Target, config: ImageConfig? = null)
    fun cleanImageCache(url: String)
    fun cleanImageCache(file: File)
    fun cleanImageCache(uri: Uri)
}