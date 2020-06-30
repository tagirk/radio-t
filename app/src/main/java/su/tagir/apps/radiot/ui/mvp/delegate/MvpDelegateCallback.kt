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

import su.tagir.apps.radiot.ui.mvp.MvpPresenter
import su.tagir.apps.radiot.ui.mvp.MvpView


/**
 * The MvpDelegate callback that will be called from [ ] or [ViewGroupMvpDelegate]. This interface must be implemented by all
 * Fragment or android.view.View that you want to support Mosby mvp.
 *
 * @param <V> The type of [MvpView]
 * @param <P> The type of [MvpPresenter]
 * @author Hannes Dorfmann
 * @since 1.1.0
</P></V> */
interface MvpDelegateCallback<V : MvpView, P : MvpPresenter<V>> {
    /**
     * Creates the presenter instance
     *
     * @return the created presenter instance
     */
    fun createPresenter(): P

    var presenter: P

    /**
     * Gets the MvpView for the presenter
     *
     * @return The view associated with the presenter
     */
    val mvpView: V
}