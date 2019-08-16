package edu.phystech.iag.kaiumov.anyassecret.studio

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPage
import edu.phystech.iag.kaiumov.anyassecret.R

// https://github.com/AppIntro/AppIntro

class StudioIntroActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val titles = resources.getStringArray(R.array.button_intro_title)
        val descriptions = resources.getStringArray(R.array.button_intro_description)
        val colors = resources.getIntArray(R.array.button_intro_bg_colors)
        val images = resources.obtainTypedArray(R.array.button_intro_images)

        for (i in 0 until titles.size) {
            val sliderPage = SliderPage()
            sliderPage.title = titles[i]
            sliderPage.description = descriptions[i]
            sliderPage.imageDrawable = images.getResourceId(i, 0)
            sliderPage.bgColor = colors[i]
            addSlide(AppIntroFragment.newInstance(sliderPage))
        }
        askForPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
            resources.getInteger(R.integer.persmission_slide)
        )

        images.recycle()

        setFadeAnimation()
        showSkipButton(false)
        isProgressButtonEnabled = true
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }

    override fun onBackPressed() = Unit

}
