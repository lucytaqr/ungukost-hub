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
 * If status is set to non-active or exitDateText is filled with a date on or before today,
 * the tenant's status is automatically "Non Aktif".
 */
fun Tenant.computedStatus(): String {
    if (status.equals("Tidak Aktif", ignoreCase = true) ||
        status.equals("Keluar", ignoreCase = true) ||
        status.equals("Non Aktif", ignoreCase = true)) {
        return "Non Aktif"
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
                val todayEnd = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                if (!exitDate.after(todayEnd.time)) {
                    return "Non Aktif"
                }
            } else {
                return "Non Aktif"
            }
        } catch (_: Exception) {
            return "Non Aktif"
        }
    }
    return if (status.isBlank()) "Aktif" else status
}

fun Tenant.isActive(): Boolean = computedStatus() == "Aktif"
