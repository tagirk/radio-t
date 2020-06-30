package androidx.core.app

import androidx.fragment.app.Fragment
import java.io.PrintWriter
import java.io.StringWriter

/**
 * With this utility class one can access the fragment backstack
 *
 * @author Hannes Dorfmann
 */
class BackstackAccessor private constructor() {
    companion object {
        /**
         * Checks whether or not a given fragment is on the backstack of the fragment manager (could also
         * be on top of the backstack and hence visible)
         *
         * @param fragment The fragment you want to check if its on the back stack
         * @return true, if the given Fragment is on the back stack, otherwise false (not on the back
         * stack)
         */
        fun isFragmentOnBackStack(fragment: Fragment): Boolean {
            val writer = StringWriter()
            fragment.dump("", null, PrintWriter(writer), null)
            val dump = writer.toString()
            return !dump.contains("mBackStackNesting=0")
        }
    }

    init {
        throw IllegalStateException("Not instantiatable")
    }
}