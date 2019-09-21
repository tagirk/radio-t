package su.tagir.apps.radiot.ui.settings

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import su.tagir.apps.radiot.R
import su.tagir.apps.radiot.di.Injectable

class CreditsFragment: Fragment(), Injectable{

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_credits, container, false)

        val pirateIconCredit = v.findViewById<TextView>(R.id.pirate_icon_credit)
        pirateIconCredit.movementMethod = LinkMovementMethod.getInstance()
        return v
    }
}