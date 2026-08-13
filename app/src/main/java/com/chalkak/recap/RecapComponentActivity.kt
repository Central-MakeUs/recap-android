package com.chalkak.recap

import android.content.Context
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import com.chalkak.recap.app.capFontScale
import com.chalkak.recap.app.withCappedFontScale

/**
 * Base activity that caps [Configuration.fontScale] at 1.5.
 * All app-owned activities should extend this.
 */
open class RecapComponentActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withCappedFontScale())
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.capFontScale()
        super.applyOverrideConfiguration(overrideConfiguration)
    }
}
