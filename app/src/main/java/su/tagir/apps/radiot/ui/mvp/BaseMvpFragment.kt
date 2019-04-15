package su.tagir.apps.radiot.ui.mvp

import android.os.Bundle
import androidx.fragment.app.Fragment

abstract class BaseMvpFragment<V: MvpView, P: MvpPresenter<V>>: Fragment(), MvpView {

    private lateinit var presenter: P

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter = createPresenter()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.saveState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            presenter.restoreState(savedInstanceState)
        }
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
}