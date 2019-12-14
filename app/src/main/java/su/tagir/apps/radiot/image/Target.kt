package su.tagir.apps.radiot.image

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

interface Target {

    fun onLoaded(bitmap: Bitmap)

    fun onError(t: Throwable?, errorDrawable: Drawable?){

    }

    fun onPrepare(placeHolder: Drawable?){

    }
}