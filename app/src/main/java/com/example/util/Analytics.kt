package com.example.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * WorkCore Analytics
 * Tracks key user actions to understand which features are used most.
 * View reports at: Firebase Console → Analytics → Events
 */
object Analytics {

    private val analytics: FirebaseAnalytics by lazy { Firebase.analytics }

    // ── Authentication ────────────────────────────────────────────────

    fun logLogin(team: String, company: String) {
        analytics.logEvent("wc_login", Bundle().apply {
            putString("team", team)
            putString("company", company) // "SS" or "AO"
        })
    }

    fun logLogout(team: String) {
        analytics.logEvent("wc_logout", Bundle().apply {
            putString("team", team)
        })
    }

    fun logAdminLogin() {
        analytics.logEvent("wc_admin_login", Bundle.EMPTY)
    }

    // ── EOD Submission ────────────────────────────────────────────────

    fun logEodSubmitted(
        employeeId: String,
        team: String,
        formType: String,   // "STANDARD", "MARKETING", "COUNSELLOR", "TELECALLING"
        isEdit: Boolean
    ) {
        analytics.logEvent("wc_eod_submitted", Bundle().apply {
            putString("employee_id", employeeId)
            putString("team", team)
            putString("form_type", formType)
            putBoolean("is_edit", isEdit)
        })
    }

    fun logEodFormOpened(team: String, formType: String) {
        analytics.logEvent("wc_eod_form_opened", Bundle().apply {
            putString("team", team)
            putString("form_type", formType)
        })
    }

    // ── Screen Views ──────────────────────────────────────────────────

    fun logScreenView(screenName: String, role: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString("role", role)
        })
    }

    // ── Admin Actions ─────────────────────────────────────────────────

    fun logAdminFilterApplied(teamFilter: String) {
        analytics.logEvent("wc_admin_filter", Bundle().apply {
            putString("team", teamFilter)
        })
    }

    fun logReportDownloaded(team: String, format: String, scope: String) {
        analytics.logEvent("wc_report_downloaded", Bundle().apply {
            putString("team", team)
            putString("format", format) // "CSV" or "Excel"
            putString("scope", scope)   // "Whole team" or "One employee"
        })
    }

    fun logEmployeeAdded(team: String, company: String) {
        analytics.logEvent("wc_employee_added", Bundle().apply {
            putString("team", team)
            putString("company", company)
        })
    }

    fun logEmployeeDeleted() {
        analytics.logEvent("wc_employee_deleted", Bundle.EMPTY)
    }

    fun logFirebaseReset() {
        analytics.logEvent("wc_firebase_reset", Bundle.EMPTY)
    }

    // ── History Page ──────────────────────────────────────────────────

    fun logHistoryViewed(team: String, filterType: String) {
        analytics.logEvent("wc_history_viewed", Bundle().apply {
            putString("team", team)
            putString("filter", filterType) // "This month", "Previous month", "Custom"
        })
    }

    // ── App Updates ───────────────────────────────────────────────────

    fun logUpdatePromptShown(newVersion: String) {
        analytics.logEvent("wc_update_prompt_shown", Bundle().apply {
            putString("new_version", newVersion)
        })
    }

    fun logUpdateAccepted(newVersion: String) {
        analytics.logEvent("wc_update_accepted", Bundle().apply {
            putString("new_version", newVersion)
        })
    }

    fun logUpdateDismissed(newVersion: String) {
        analytics.logEvent("wc_update_dismissed", Bundle().apply {
            putString("new_version", newVersion)
        })
    }
}
