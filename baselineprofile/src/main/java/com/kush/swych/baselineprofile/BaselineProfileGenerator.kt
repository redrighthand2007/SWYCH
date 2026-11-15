package com.kush.swych.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Records which code runs while Swych starts and shows its first screens. The result is
 * saved into the app, and Android compiles that code ahead of time when the app is installed.
 *
 * Run with a phone connected:  ./gradlew :app:generateBaselineProfile
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = PACKAGE_NAME, includeInStartupProfile = true) {
        pressHome()
        startActivityAndWait()

        // Fresh install, so the welcome screen shows. Open login and sign-up too.
        device.wait(Until.hasObject(By.text("Login")), 5_000)
        device.findObject(By.text("Login"))?.let {
            it.click()
            device.waitForIdle()
            device.pressBack()
            device.waitForIdle()
        }
        device.findObject(By.textContains("New To"))?.let {
            it.click()
            device.waitForIdle()
            device.pressBack()
        }
    }
}
