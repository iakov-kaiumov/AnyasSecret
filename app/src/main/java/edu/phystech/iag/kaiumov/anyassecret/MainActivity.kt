package edu.phystech.iag.kaiumov.anyassecret

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import edu.phystech.iag.kaiumov.anyassecret.studio.StudioActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val IS_GAMING_KEY = "edu.phystech.iag.kaiumov.is_gaming"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isGaming = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
            IS_GAMING_KEY,
            true
        )
        startActivity(
            Intent(
                applicationContext,
                if (isGaming) StudioActivity::class.java else NightActivity::class.java
            )
        )

        finish()
    }
}
