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

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import su.tagir.apps.radiot.ui.mvp.MvpPresenter
import su.tagir.apps.radiot.ui.mvp.MvpView


/**
 * A delegate for Fragments to attach them to mosby mvp.
 *
 *
 * The following methods must be invoked from the corresponding Fragments lifecycle methods:
 *
 *
 *  * [.onCreate]
 *  * [.onDestroy]
 *  * [.onPause]
 *  * [.onResume]
 *  * [.onStart]
 *  * [.onStop]
 *  * [.onViewCreated]
 *  * [.onActivityCreated]
 *  * [.onSaveInstanceState]
 *  * [.onAttach]
 *  * [.onAttach]
 *  * [.onDetach]
 *  *
 *
 *
 *
 * @param <V> The type of [MvpView]
 * @param <P> The type of [MvpPresenter]
 * @author Hannes Dorfmann
 * @since 1.1.0
</P></V> */
interface FragmentMvpDelegate<V : MvpView, P : MvpPresenter<V>> {
    /**
     * Must be called from [Fragment.onCreate]
     *
     * @param saved The bundle
     */
    fun onCreate(saved: Bundle?)

    /**
     * Must be called from [Fragment.onDestroy]
     */
    fun onDestroy()

    /**
     * Must be called from [Fragment.onViewCreated]
     *
     * @param view The inflated view
     * @param savedInstanceState the bundle with the viewstate
     */
    fun onViewCreated(view: View?, savedInstanceState: Bundle?)

    /**
     * Must be called from [Fragment.onDestroyView]
     */
    fun onDestroyView()

    /**
     * Must be called from [Fragment.onPause]
     */
    fun onPause()

    /**
     * Must be called from [Fragment.onResume]
     */
    fun onResume()

    /**
     * Must be called from [Fragment.onStart]
     */
    fun onStart()

    /**
     * Must be called from [Fragment.onStop]
     */
    fun onStop()

    /**
     * Must be called from [Fragment.onAttach]
     *
     * @param context The context the fragment is attached to
     */
    fun onAttach(context: Context?)

    /**
     * Must be called from [Fragment.onDetach]
     */
    fun onDetach()

    /**
     * Must be called from [Fragment.onSaveInstanceState]
     */
    fun onSaveInstanceState(outState: Bundle?)
}
