package su.tagir.apps.radiot.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder


abstract class BaseFragment : Fragment() {

    private lateinit var unbinder: Unbinder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       val v = createView(inflater, container)
        unbinder = ButterKnife.bind(this, v)
        return v
    }

    override fun onDestroyView() {
        unbinder.unbind()
        super.onDestroyView()
    }

    abstract fun createView(inflater: LayoutInflater, container: ViewGroup?): View

    protected fun showToast(message:String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}