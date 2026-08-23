package com.luma.focus.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.luma.focus.data.LumaStore

/**
 * App blocking, implemented the permission-light way: instead of drawing an overlay
 * (which needs SYSTEM_ALERT_WINDOW and extra user setup), this just detects when a
 * blocked app comes to the foreground and immediately returns the user to the home
 * screen. Simple, and needs no permission beyond enabling the Accessibility Service itself.
 *
 * IMPORTANT — this must be manually turned on once, after install, by the user:
 * Settings > Accessibility > Luma > enable it.
 * This is an Android platform requirement, not something the app can do automatically.
 */
class LumaAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        LumaStore.init(applicationContext)
        val blocked = LumaStore.getBlockedApps()
        if (packageName in blocked) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onInterrupt() {}
}
