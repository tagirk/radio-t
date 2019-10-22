package su.tagir.apps.radiot.image

import android.content.Context
import androidx.annotation.DrawableRes

class ImageConfig(@DrawableRes var placeholder: Int? = null,

                  @DrawableRes var error: Int? = null,

                  var centerCrop: Boolean = false,

                  var centerInside: Boolean = false,

                  var fit: Boolean = false,

                  var supportMemoryCache: Boolean = true,

                  var retreiveFromCacheOnly: Boolean = false,

                  var retreiveIngnoringCache: Boolean = false,

                  var fadeIn: Int = 200,

                  vararg transformations: Transformation,

                  var context: Context? = null)



