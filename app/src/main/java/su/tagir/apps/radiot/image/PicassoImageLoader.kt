package su.tagir.apps.radiot.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import java.io.File

object PicassoImageLoader : IImageLoader {

    override fun display(res: Int, imageView: ImageView, config: ImageConfig?) =
            getInstance(config?.context)
                    .load(res)
                    .applyConfig(config)
                    .into(imageView)

    override fun display(url: String, imageView: ImageView, config: ImageConfig?) =
            getInstance(config?.context)
                    .load(url)
                    .applyConfig(config)
                    .into(imageView)

    override fun display(file: File, imageView: ImageView, config: ImageConfig?) =
            getInstance(config?.context)
                    .load(file)
                    .applyConfig(config)
                    .into(imageView)

    override fun display(uri: Uri, imageView: ImageView, config: ImageConfig?) =
            getInstance(config?.context)
                    .load(uri)
                    .applyConfig(config).into(imageView)


    override fun load(url: String, target: Target, config: ImageConfig?) =
            getInstance(config?.context)
                    .load(url)
                    .applyConfig(config)
                    .into(createPicassoTarget(target))


    override fun load(file: File, target: Target, config: ImageConfig?) =
            getInstance(config?.context)
                    .load(file)
                    .applyConfig(config)
                    .into(createPicassoTarget(target))


    override fun load(uri: Uri, target: Target, config: ImageConfig?) =
            getInstance(config?.context)
                    .load(uri)
                    .applyConfig(config)
                    .into(createPicassoTarget(target))


    override fun cleanImageCache(url: String) = Picasso.get().invalidate(url)

    override fun cleanImageCache(file: File) = Picasso.get().invalidate(file)

    override fun cleanImageCache(uri: Uri) = Picasso.get().invalidate(uri)

    private fun createPicassoTarget(target: Target): com.squareup.picasso.Target =
            object : com.squareup.picasso.Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    target.onPrepare(placeHolderDrawable)
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    target.onError(e, errorDrawable)
                }

                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    bitmap?.let {
                        target.onLoaded(bitmap)
                    }
                }

            }

    private fun getInstance(context: Context?): Picasso =
            context?.let{ c -> Picasso
                    .Builder(c.applicationContext)
                    .build()
            } ?: Picasso.get()

}


fun RequestCreator.applyConfig(config: ImageConfig?): RequestCreator {
    config?.let {
        if (config.centerCrop) {
            this.centerCrop()
        }

        if (config.centerInside) {
            this.centerInside()
        }

        if (config.fit) {
            this.fit()
        }

        if (!config.supportMemoryCache) {
            this.memoryPolicy(MemoryPolicy.NO_STORE)
            this.networkPolicy(NetworkPolicy.NO_STORE)
        }

        if(config.retreiveFromCacheOnly){
            this.networkPolicy(NetworkPolicy.OFFLINE)
        }

        if (config.retreiveIngnoringCache) {
            this.networkPolicy(NetworkPolicy.NO_CACHE)
            this.memoryPolicy(MemoryPolicy.NO_CACHE)

        }

        config.placeholder?.let { placeholder -> this.placeholder(placeholder) }
        config.error?.let { error -> this.error(error) }
    }

    return this
}