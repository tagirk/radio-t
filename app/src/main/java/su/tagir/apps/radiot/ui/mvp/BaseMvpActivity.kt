package su.tagir.apps.radiot.ui.mvp

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

abstract class BaseMvpActivity<V: MvpView, P: MvpPresenter<V>>: AppCompatActivity(), MvpView {

    protected lateinit var presenter: P

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = createPresenter()
    }

    override fun onStart() {
        super.onStart()
        presenter.attachView(this as V)
    }

    override fun onStop() {
        presenter.detachView()
        super.onStop()
    }

    abstract fun createPresenter(): P

    override fun showError(t: Throwable) {
            AlertDialog.Builder(this)
                    .setTitle("Ошибка")
                    .setMessage(t.message)
                    .setPositiveButton("OK", null)
                    .create()
                    .show()
    }

    override fun showProgress(show: Boolean) {

    }
}