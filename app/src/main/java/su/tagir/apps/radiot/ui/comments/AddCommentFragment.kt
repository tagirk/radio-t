package su.tagir.apps.radiot.ui.comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.model.entries.Comment

class AddCommentFragment: BottomSheetDialogFragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_add_comment, container, false)


    companion object {

        const val KEY_REPLY = "reply"

        fun newInstance(comment: Comment?): AddCommentFragment{
            val args = Bundle()
            args.putParcelable(KEY_REPLY, comment)
            val fr = AddCommentFragment()
            fr.arguments = args
            return fr
        }
    }
}