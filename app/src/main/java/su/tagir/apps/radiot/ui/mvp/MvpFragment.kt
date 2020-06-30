package su.tagir.apps.radiot.ui.mvp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import su.tagir.apps.radiot.ui.mvp.delegate.FragmentMvpDelegate
import su.tagir.apps.radiot.ui.mvp.delegate.FragmentMvpDelegateImpl
import su.tagir.apps.radiot.ui.mvp.delegate.MvpDelegateCallback


abstract class MvpFragment<V: MvpView, P: MvpPresenter<V>>: Fragment, MvpDelegateCallback<V, P>, MvpView {

    override lateinit var presenter: P

    override val mvpView: V
        get() = this as V

    protected var delegate: FragmentMvpDelegate<V, P>? = null

    constructor(): super()

    constructor(@LayoutRes layoutId: Int): super(layoutId)

    protected open fun getMvpDelegate(): FragmentMvpDelegate<V, P> {
        if (delegate == null){
           delegate = FragmentMvpDelegateImpl(this, this, keepPresenterInstanceDuringScreenOrientationChanges = true, keepPresenterOnBackstack = true)
        }
        return delegate!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getMvpDelegate().onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        getMvpDelegate().onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMvpDelegate().onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getMvpDelegate().onDestroyView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getMvpDelegate().onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
        getMvpDelegate().onDetach()
    }

    override fun onStart() {
        super.onStart()
        getMvpDelegate().onStart()
    }

    override fun onStop() {
        super.onStop()
        getMvpDelegate().onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        getMvpDelegate().onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        getMvpDelegate().onResume()
    }

    override fun onPause() {
        super.onPause()
        getMvpDelegate().onPause()
    }


    override fun showError(t: Throwable) {
        context?.let {c ->
            AlertDialog.Builder(c)
                    .setTitle("Ошибка")
                    .setMessage(t.message)
                    .setPositiveButton("OK", null)
                    .create()
                    .show()
        }
    }

    override fun showProgress(show: Boolean) {

    }

    abstract override fun createPresenter(): P

    protected fun showToast(message:String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}