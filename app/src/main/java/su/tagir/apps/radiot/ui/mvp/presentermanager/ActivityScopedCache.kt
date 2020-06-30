package su.tagir.apps.radiot.ui.mvp.presentermanager


/*
 * Copyright 2017 Hannes Dorfmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import androidx.collection.ArrayMap
import su.tagir.apps.radiot.ui.mvp.MvpPresenter
import su.tagir.apps.radiot.ui.mvp.MvpView


/**
 * This class basically represents a Map for View Id to the Presenter / ViewState.
 * One instance of this class is also associated by [PresenterManager] to one Activity (kept
 * across screen orientation changes)
 *
 * @author Hannes Dorfmann
 * @since 3.0.0
 */
internal class ActivityScopedCache {
    private val presenterMap: MutableMap<String, PresenterHolder> = ArrayMap()
    fun clear() {
        presenterMap.clear()
    }


    fun <P> getPresenter(viewId: String): P? {
        val holder = presenterMap[viewId]
        return if (holder == null) null else holder.presenter as P?
    }


    fun <VS> getViewState(viewId: String): VS? {
        val holder = presenterMap[viewId]
        return if (holder == null) null else holder.viewState as VS?
    }


    fun putPresenter(viewId: String,
                     presenter: MvpPresenter<out MvpView>) {
        var presenterHolder = presenterMap[viewId]
        if (presenterHolder == null) {
            presenterHolder = PresenterHolder()
            presenterHolder.presenter = presenter
            presenterMap[viewId] = presenterHolder
        } else {
            presenterHolder.presenter = presenter
        }
    }


    fun putViewState(viewId: String,
                     viewState: Any) {
        var presenterHolder = presenterMap[viewId]
        if (presenterHolder == null) {
            presenterHolder = PresenterHolder()
            presenterHolder.viewState = viewState
            presenterMap[viewId] = presenterHolder
        } else {
            presenterHolder.viewState = viewState
        }
    }

    fun remove(viewId: String) {
        presenterMap.remove(viewId)
    }

    internal class PresenterHolder {
        var presenter: MvpPresenter<*>? = null
        var viewState // workaround: didn't want to introduce dependency to viewstate module
                : Any? = null
    }
}
