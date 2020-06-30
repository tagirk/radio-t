package su.tagir.apps.radiot.ui.mvp.presentermanager


/*
 * Copyright 2016 Hannes Dorfmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
import android.os.Parcel
import android.os.Parcelable
import androidx.customview.view.AbsSavedState


/**
 * The SavedState implementation to store the view's internal id to
 *
 * @author Hannes Dorfmann
 * @since 3.0
 */
class SavedState : AbsSavedState {
    var mosbyViewId: String?
        private set

    constructor(superState: Parcelable, mosbyViewId: String) : super(superState) {
        this.mosbyViewId = mosbyViewId
    }

    protected constructor(`in`: Parcel, loader: ClassLoader?) : super(`in`, loader) {
        mosbyViewId = `in`.readString()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeString(mosbyViewId)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SavedState?> = object : Parcelable.ClassLoaderCreator<SavedState?>{

            override fun createFromParcel(source: Parcel, loader: ClassLoader?): SavedState? {
                val nLoader = loader ?: SavedState::class.java.classLoader
                return SavedState(source, nLoader)
            }

            override fun createFromParcel(source: Parcel): SavedState? {
                return this.createFromParcel(source, null)
            }


            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}
