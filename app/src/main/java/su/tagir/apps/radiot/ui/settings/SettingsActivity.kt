package su.tagir.apps.radiot.ui.settings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import su.tagir.apps.radiot.R


class SettingsActivity : AppCompatActivity(){

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_settings)
        ButterKnife.bind(this)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        fragmentManager
                .beginTransaction()
                .replace(R.id.container, SettingsFragment())
                .commitAllowingStateLoss()
    }


}

