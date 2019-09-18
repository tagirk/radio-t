package su.tagir.apps.radiot.ui.mvp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

abstract class BaseMvpFragment<V: MvpView, P: MvpPresenter<V>>: Fragment(), MvpView {

    protected lateinit var presenter: P

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter = createPresenter()
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(this as V)
    }

    override fun onPause() {
        presenter.detachView()
        super.onPause()
    }

    abstract fun createPresenter(): P

    abstract fun createView(inflater: LayoutInflater, container: ViewGroup?): View

    protected fun showToast(message:String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}