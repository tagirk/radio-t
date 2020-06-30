package su.tagir.apps.radiot.ui.mvp.delegate


/*
 * Copyright 2015 Hannes Dorfmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.app.BackstackAccessor
import androidx.fragment.app.Fragment
import su.tagir.apps.radiot.ui.mvp.MvpPresenter
import su.tagir.apps.radiot.ui.mvp.MvpView
import su.tagir.apps.radiot.ui.mvp.presentermanager.PresenterManager
import timber.log.Timber
import java.util.*


/**
 * The default implementation of [FragmentMvpDelegate]
 * Presenter is available (has view that is attached) in [.onViewCreated] (after
 * super.onViewCreated() is called). View will be detached in [.onDestroyView] from presenter,
 * and eventually presenter will be destroyed in [.onDestroy].
 *
 * @param <V> The type of [MvpView]
 * @param <P> The type of [MvpPresenter]
 * @author Hannes Dorfmann
 * @see FragmentMvpDelegate
 *
 * @since 1.1.0
</P></V> */
class FragmentMvpDelegateImpl<V : MvpView, P : MvpPresenter<V>>(private val fragment: Fragment,
                                                                private val delegateCallback: MvpDelegateCallback<V, P>,
                                                                 private val  keepPresenterInstanceDuringScreenOrientationChanges: Boolean,
                                                                private val keepPresenterOnBackstack: Boolean) : FragmentMvpDelegate<V, P> {


    private var onViewCreatedCalled = false
    protected var viewId: String? = null

    /**
     * Generates the unique (mosby internal) view id and calls [ ][MvpDelegateCallback.createPresenter]
     * to create a new presenter instance
     *
     * @return The new created presenter instance
     */
    private fun createViewIdAndCreatePresenter(): P {
        val presenter = delegateCallback.createPresenter()
        if (keepPresenterInstanceDuringScreenOrientationChanges) {
            viewId = UUID.randomUUID().toString()
            PresenterManager.putPresenter(activity, viewId!!, presenter)
        }
        return presenter
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val presenter = presenter
        presenter.attachView(mvpView)
        Timber.d("View$mvpView attached to Presenter $presenter")
        onViewCreatedCalled = true
    }

    private val activity: Activity
        get() = fragment.activity
                ?: throw NullPointerException(
                        "Activity returned by Fragment.getActivity() is null. Fragment is $fragment")

    private val presenter: P
        get() {
            return delegateCallback.presenter
        }

    private val mvpView: V
        get() {
            return delegateCallback.mvpView
        }

    override fun onDestroyView() {
        onViewCreatedCalled = false
        presenter.detachView()
    }

    override fun onPause() {}
    override fun onResume() {}
    override fun onStart() {
        check(onViewCreatedCalled) {
            ("It seems that you are using "
                    + delegateCallback.javaClass.canonicalName
                    + " as headless (UI less) fragment (because onViewCreated() has not been called or maybe delegation misses that part). Having a Presenter without a View (UI) doesn't make sense. Simply use an usual fragment instead of an MvpFragment if you want to use a UI less Fragment")
        }
    }

    override fun onStop() {}
    override fun onAttach(context: Context?) {}
    override fun onDetach() {}
    override fun onSaveInstanceState(outState: Bundle?) {
        if ((keepPresenterInstanceDuringScreenOrientationChanges || keepPresenterOnBackstack)
                && outState != null) {
            outState.putString(KEY_VIEW_ID, viewId)
        }
    }

    @SuppressLint("BinaryOperationInTimber")
    override fun onCreate(saved: Bundle?) {
        var presenter: P?
        if (saved != null && keepPresenterInstanceDuringScreenOrientationChanges) {
            viewId = saved.getString(KEY_VIEW_ID)
            presenter = if(viewId != null) PresenterManager.getPresenter(activity, viewId!!) else null
            if (presenter != null) {
                //
                // Presenter restored from cache
                //
                if (DEBUG) {
                    Timber.d("Reused presenter $presenter + for view ${delegateCallback.mvpView}")
                }
            } else {
                //
                // No presenter found in cache, most likely caused by process death
                //
                presenter = createViewIdAndCreatePresenter()
                if (DEBUG) {
                    Timber.d("No presenter found although view Id was here: "
                            + viewId
                            + ". Most likely this was caused by a process death. New Presenter created"
                            + presenter
                            + " for view "
                            + mvpView)
                }
            }
        } else {
            //
            // Activity starting first time, so create a new presenter
            //
            presenter = createViewIdAndCreatePresenter()
            if (DEBUG) {
                Timber.d("New presenter $presenter for view $mvpView")
            }
        }
        delegateCallback.presenter = presenter
    }

    @SuppressLint("BinaryOperationInTimber")
    override fun onDestroy() {
        val activity = activity
        val retainPresenterInstance = retainPresenterInstance(activity, fragment,
                keepPresenterInstanceDuringScreenOrientationChanges, keepPresenterOnBackstack)
        val presenter = presenter
        if (!retainPresenterInstance) {
            presenter.destroy()
            if (DEBUG) {
                Timber.d("Presenter destroyed. MvpView "
                        + delegateCallback.mvpView
                        + "   Presenter: "
                        + presenter)
            }
        }
        if (!retainPresenterInstance && viewId != null) {
            // mosbyViewId is null if keepPresenterInstanceDuringScreenOrientationChanges  == false
            PresenterManager.remove(activity, viewId!!)
        }
    }

    companion object {
        private const val KEY_VIEW_ID = "su.tagir.apps.radiot.ui.mvp.fragment.id"
        var DEBUG = false
        fun retainPresenterInstance(activity: Activity, fragment: Fragment,
                                    keepPresenterInstanceDuringScreenOrientationChanges: Boolean,
                                    keepPresenterOnBackstack: Boolean): Boolean {
            if (activity.isChangingConfigurations) {
                return keepPresenterInstanceDuringScreenOrientationChanges
            }
            if (activity.isFinishing) {
                return false
            }
            return if (keepPresenterOnBackstack && BackstackAccessor.isFragmentOnBackStack(fragment)) {
                true
            } else !fragment.isRemoving
        }
    }


}
