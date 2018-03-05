package su.tagir.apps.radiot.ui.common

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.Unbinder


abstract class BaseFragment : Fragment() {

    private lateinit var unbinder: Unbinder

    private var viewLifecycleOwner: ViewLifecycleOwner? = null

    fun getViewLifecycleOwner(): LifecycleOwner? {
        return viewLifecycleOwner
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       val v = createView(inflater, container)
        unbinder = ButterKnife.bind(this, v)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner = ViewLifecycleOwner()
        viewLifecycleOwner!!.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onStart() {
        super.onStart()
            viewLifecycleOwner?.lifecycle?.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner?.lifecycle?.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

    }

    override fun onPause() {
        viewLifecycleOwner?.lifecycle?.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        super.onPause()
    }

    override fun onStop() {
        viewLifecycleOwner?.lifecycle?.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        super.onStop()
    }

    override fun onDestroyView() {
        viewLifecycleOwner?.lifecycle?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewLifecycleOwner = null
        unbinder.unbind()
        super.onDestroyView()
    }

    abstract fun createView(inflater: LayoutInflater, container: ViewGroup?): View

    open fun onBackPressed(){}

    protected fun showToast(message:String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private class ViewLifecycleOwner : LifecycleOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)

        override fun getLifecycle(): LifecycleRegistry {
            return lifecycleRegistry
        }
    }
}