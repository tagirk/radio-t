package su.tagir.apps.radiot.ui.mvp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

    abstract fun createPresenter(): P

    protected fun showToast(message:String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}