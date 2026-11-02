package com.example.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * WorkCore Analytics
 * Tracks key user actions to understand which features are used most.
 * View reports at: Firebase Console → Analytics → Events
 */
object Analytics {

    private var analytics: FirebaseAnalytics? = null

    // Call once from WorkCoreApplication.onCreate()
    fun init(context: Context) {
        analytics = FirebaseAnalytics.getInstance(context)
    }

    private fun log(event: String, params: Bundle = Bundle.EMPTY) {
        analytics?.logEvent(event, params)
    }

    // ── Authentication ────────────────────────────────────────────────

    fun logLogin(team: String, company: String) {
        log("wc_login", Bundle().apply {
            putString("team", team)
            putString("company", company)
        })
    }

    fun logLogout(team: String) {
        log("wc_logout", Bundle().apply {
            putString("team", team)
        })
    }

    fun logAdminLogin() {
        log("wc_admin_login", Bundle.EMPTY)
    }

    // ── EOD Submission ────────────────────────────────────────────────

    fun logEodSubmitted(
        employeeId: String,
        team: String,
        formType: String,
        isEdit: Boolean
    ) {
        log("wc_eod_submitted", Bundle().apply {
            putString("employee_id", employeeId)
            putString("team", team)
            putString("form_type", formType)
            putBoolean("is_edit", isEdit)
        })
    }

    fun logEodFormOpened(team: String, formType: String) {
        log("wc_eod_form_opened", Bundle().apply {
            putString("team", team)
            putString("form_type", formType)
        })
    }

    // ── Screen Views ──────────────────────────────────────────────────

    fun logScreenView(screenName: String, role: String) {
        log(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString("role", role)
        })
    }

    // ── Admin Actions ─────────────────────────────────────────────────

    fun logAdminFilterApplied(teamFilter: String) {
        log("wc_admin_filter", Bundle().apply {
            putString("team", teamFilter)
        })
    }

    fun logReportDownloaded(team: String, format: String, scope: String) {
        log("wc_report_downloaded", Bundle().apply {
            putString("team", team)
            putString("format", format)
            putString("scope", scope)
        })
    }

    fun logEmployeeAdded(team: String, company: String) {
        log("wc_employee_added", Bundle().apply {
            putString("team", team)
            putString("company", company)
        })
    }

    fun logEmployeeDeleted() {
        log("wc_employee_deleted", Bundle.EMPTY)
    }

    // ── History Page ──────────────────────────────────────────────────

    fun logHistoryViewed(team: String, filterType: String) {
        log("wc_history_viewed", Bundle().apply {
            putString("team", team)
            putString("filter", filterType)
        })
    }

    // ── App Updates ───────────────────────────────────────────────────

    fun logUpdatePromptShown(newVersion: String) {
        log("wc_update_prompt_shown", Bundle().apply {
            putString("new_version", newVersion)
        })
    }

    fun logUpdateAccepted(newVersion: String) {
        log("wc_update_accepted", Bundle().apply {
            putString("new_version", newVersion)
        })
    }

    fun logUpdateDismissed(newVersion: String) {
        log("wc_update_dismissed", Bundle().apply {
            putString("new_version", newVersion)
        })
    }
}
