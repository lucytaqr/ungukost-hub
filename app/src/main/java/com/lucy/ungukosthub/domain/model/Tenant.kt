package com.lucy.ungukosthub.domain.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Domain Model representing a Tenant entity in Ungu Kost.
 */
data class Tenant(
    val id: String = "",
    val name: String = "",
    val origin: String = "",
    val birthDate: String = "",
    val phone: String = "",
    val emergencyContact: String = "",
    val roomId: String = "",
    val roomNumber: String = "",
    val ktpUrl: String = "",
    val status: String = "Aktif",
    val entryDate: Long = System.currentTimeMillis(),
    val entryDateText: String = "",
    val exitDateText: String = ""
)

/**
 * Helper to compute effective tenant status.
 * If exitDateText is filled AND the exit date is before today (00:00:00),
 * the tenant's status is automatically "Tidak Aktif".
 */
fun Tenant.computedStatus(): String {
    if (status.equals("Tidak Aktif", ignoreCase = true) || status.equals("Keluar", ignoreCase = true)) {
        return "Tidak Aktif"
    }
    if (exitDateText.isNotBlank()) {
        try {
            val formats = listOf(
                SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")),
                SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")),
                SimpleDateFormat("dd MMMM yyyy", Locale.US),
                SimpleDateFormat("dd MMM yyyy", Locale.US)
            )
            var exitDate: Date? = null
            for (fmt in formats) {
                try {
                    val parsed = fmt.parse(exitDateText)
                    if (parsed != null) {
                        exitDate = parsed
                        break
                    }
                } catch (_: Exception) {}
            }
            if (exitDate != null) {
                val todayCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (exitDate.before(todayCal.time)) {
                    return "Tidak Aktif"
                }
            }
        } catch (_: Exception) {}
    }
    return status.ifBlank { "Aktif" }
}

fun Tenant.isActive(): Boolean = computedStatus() == "Aktif"
