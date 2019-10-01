package su.tagir.apps.radiot.model.entries

import android.os.Parcel
import android.os.Parcelable


data class Progress(var duration: Long = 0L,
                    var progress: Long = 0L) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readLong())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(duration)
        parcel.writeLong(progress)
    }

    fun readFromParcel(`in`: Parcel) {
        duration = `in`.readLong()
        progress = `in`.readLong()
    }


    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Progress> {
        override fun createFromParcel(parcel: Parcel): Progress {
            return Progress(parcel)
        }

        override fun newArray(size: Int): Array<Progress?> {
            return arrayOfNulls(size)
        }
    }
}


