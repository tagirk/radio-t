package su.tagir.apps.radiot.ui.mvp.presentermanager

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.collection.ArrayMap
import su.tagir.apps.radiot.ui.mvp.MvpPresenter
import su.tagir.apps.radiot.ui.mvp.MvpView
import timber.log.Timber
import java.util.*

/**
 * A internal class responsible to save internal presenter instances during screen orientation
 * changes and reattach the presenter afterwards.
 *
 * <p>
 * The idea is that each MVP View (like a Activity, Fragment, ViewGroup) will get a unique view id.
 * This view id is
 * used to store the presenter and viewstate in it. After screen orientation changes we can reuse
 * the presenter and viewstate by querying for the given view id (must be saved in view's state
 * somehow).
 * </p>
 *
 * @author Hannes Dorfmann
 * @since 3.0
 */

object PresenterManager {

        const val KEY_ACTIVITY_ID = "su.tagir.apps.radiot.ui.mvp.PresenterManagerActivityId"
        private val activityIdMap: MutableMap<Activity, String> = ArrayMap()
        private val activityScopedCacheMap: MutableMap<String, ActivityScopedCache?> = ArrayMap()

        private val activityLifecycleCallbacks: ActivityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (savedInstanceState != null) {
                    val activityId = savedInstanceState.getString(KEY_ACTIVITY_ID)
                    if (activityId != null) {
                        // After a screen orientation change we map the newly created Activity to the same
                        // Activity ID as the previous activity has had (before screen orientation change)
                        activityIdMap[activity] = activityId
                    }
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                // Save the activityId into bundle so that the other
                val activityId = activityIdMap[activity]
                if (activityId != null) {
                    outState.putString(KEY_ACTIVITY_ID, activityId)
                }
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (!activity.isChangingConfigurations) {
                    // Activity will be destroyed permanently, so reset the cache
                    val activityId = activityIdMap[activity]
                    if (activityId != null) {
                        val scopedCache: ActivityScopedCache? = activityScopedCacheMap[activityId]
                        if (scopedCache != null) {
                            scopedCache.clear()
                            activityScopedCacheMap.remove(activityId)
                        }

                        // No Activity Scoped cache available, so unregister
                        if (activityScopedCacheMap.isEmpty()) {
                            // All Mosby related activities are destroyed, so we can remove the activity lifecylce listener
                            activity.application
                                    .unregisterActivityLifecycleCallbacks(this)
                            Timber.d("Unregistering ActivityLifecycleCallbacks")
                        }
                    }
                }
                activityIdMap.remove(activity)
            }
        }

        /**
         * Get an already existing [ActivityScopedCache] or creates a new one if not existing yet
         *
         * @param activity The Activitiy for which you want to get the activity scope for
         * @return The [ActivityScopedCache] for the given Activity
         */
        @MainThread
        internal fun getOrCreateActivityScopedCache(
                activity: Activity): ActivityScopedCache {
            var activityId = activityIdMap[activity]
            if (activityId == null) {
                // Activity not registered yet
                activityId = UUID.randomUUID().toString()
                activityIdMap[activity] = activityId
                if (activityIdMap.size == 1) {
                    // Added the an Activity for the first time so register Activity LifecycleListener
                    activity.application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
                    Timber.d("Registering ActivityLifecycleCallbacks")
                }
            }
            var activityScopedCache: ActivityScopedCache? = activityScopedCacheMap[activityId]
            if (activityScopedCache == null) {
                activityScopedCache = ActivityScopedCache()
                activityScopedCacheMap[activityId] = activityScopedCache
            }
            return activityScopedCache
        }

        /**
         * Get the  [ActivityScopedCache] for the given Activity or `null` if no [ ] exists for the given Activity
         *
         * @param activity The activity
         * @return The [ActivityScopedCache] or null
         * @see .getOrCreateActivityScopedCache
         */

        @MainThread
        internal fun getActivityScope(activity: Activity): ActivityScopedCache? {
            val activityId = activityIdMap[activity] ?: return null
            return activityScopedCacheMap[activityId]
        }

        /**
         * Get the presenter for the View with the given (Mosby - internal) view Id or `null`
         * if no presenter for the given view (via view id) exists.
         *
         * @param activity The Activity (used for scoping)
         * @param viewId The mosby internal View Id (unique among all [MvpView]
         * @param <P> The Presenter type
         * @return The Presenter or `null`
        </P> */

        internal fun <P> getPresenter(activity: Activity, viewId: String): P? {
            return getActivityScope(activity)?.getPresenter(viewId)
        }

        /**
         * Get the ViewState (see mosby viestate modlue) for the View with the given (Mosby - internal)
         * view Id or `null`
         * if no viewstate for the given view exists.
         *
         * @param activity The Activity (used for scoping)
         * @param viewId The mosby internal View Id (unique among all [MvpView]
         * @param <VS> The type of the ViewState type
         * @return The Presenter or `null`
        </VS> */
        internal fun <VS> getViewState(activity: Activity, viewId: String): VS? {
            val scopedCache: ActivityScopedCache? = getActivityScope(activity)
            return scopedCache?.getViewState(viewId)
        }

        /**
         * Get the Activity of a context. This is typically used to determine the hosting activity of a
         * [View]
         *
         * @param context The context
         * @return The Activity or throws an Exception if Activity couldnt be determined
         */
        internal fun getActivity(context: Context): Activity {
            var ctx = context
            if (ctx is Activity) {
                return ctx
            }
            while (ctx is ContextWrapper) {
                if (ctx is Activity) {
                    return ctx
                }
                ctx = ctx.baseContext
            }
            throw IllegalStateException("Could not find the surrounding Activity")
        }

        /**
         * Clears the internal (static) state. Used for testing.
         */
        internal fun reset() {
            activityIdMap.clear()
            for (scopedCache in activityScopedCacheMap.values) {
                scopedCache?.clear()
            }
            activityScopedCacheMap.clear()
        }

        /**
         * Puts the presenter into the internal cache
         *
         * @param activity The parent activity
         * @param viewId the view id (mosby internal)
         * @param presenter the presenter
         */
        internal fun putPresenter(activity: Activity, viewId: String,
                         presenter: MvpPresenter<out MvpView>) {
            val scopedCache: ActivityScopedCache = getOrCreateActivityScopedCache(activity)
            scopedCache.putPresenter(viewId, presenter)
        }

        /**
         * Puts the presenter into the internal cache
         *
         * @param activity The parent activity
         * @param viewId the view id (mosby internal)
         * @param viewState the presenter
         */
        internal fun putViewState(activity: Activity, viewId: String,
                         viewState: Any) {
            val scopedCache: ActivityScopedCache = getOrCreateActivityScopedCache(activity)
            scopedCache.putViewState(viewId, viewState)
        }

        /**
         * Removes the Presenter (and ViewState) for the given View. Does nothing if no Presenter is
         * stored internally with the given viewId
         *
         * @param activity The activity
         * @param viewId The mosby internal view id
         */
        internal fun remove(activity: Activity, viewId: String) {
            val activityScope: ActivityScopedCache? = getActivityScope(activity)
            activityScope?.remove(viewId)

        }
}



