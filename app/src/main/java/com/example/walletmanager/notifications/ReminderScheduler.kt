package com.example.walletmanager.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDateTime
import java.time.Month
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID

data class ReminderDraft(
    val title: String,
    val message: String,
    val triggerAtMillis: Long,
    val monthlyDay: Int? = null,
    val yearlyMonth: Int? = null,
    val yearlyDay: Int? = null,
    val hourOfDay: Int = 9,
    val minute: Int = 0,
    val id: String = UUID.randomUUID().toString()
)

object ReminderScheduler {
    private const val preferencesName = "monthly_reminders"
    private const val yearlyPreferencesName = "yearly_reminders"
    private const val savedPreferencesName = "saved_reminders"
    private const val reminderIdsKey = "ids"

    fun schedule(context: Context, reminder: ReminderDraft) {
        saveReminder(context, reminder)
        if (reminder.monthlyDay != null) {
            saveMonthlyReminder(context, reminder, reminder.monthlyDay)
        } else {
            removeSavedReminder(context, preferencesName, reminder.id)
        }
        if (reminder.yearlyMonth != null && reminder.yearlyDay != null) {
            saveYearlyReminder(context, reminder, reminder.yearlyMonth, reminder.yearlyDay)
        } else {
            removeSavedReminder(context, yearlyPreferencesName, reminder.id)
        }
        val triggerAtMillis = when {
            reminder.monthlyDay != null -> nextMonthlyTrigger(
                reminder.monthlyDay,
                reminder.hourOfDay,
                reminder.minute,
                System.currentTimeMillis()
            )
            reminder.yearlyMonth != null && reminder.yearlyDay != null -> nextYearlyTrigger(
                reminder.yearlyMonth,
                reminder.yearlyDay,
                reminder.hourOfDay,
                reminder.minute,
                System.currentTimeMillis()
            )
            else -> reminder.triggerAtMillis
        }
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.titleKey, reminder.title)
            putExtra(ReminderReceiver.messageKey, reminder.message)
            putExtra(ReminderReceiver.idKey, reminder.id)
            putExtra(ReminderReceiver.monthlyDayKey, reminder.monthlyDay ?: 0)
            putExtra(ReminderReceiver.yearlyMonthKey, reminder.yearlyMonth ?: 0)
            putExtra(ReminderReceiver.yearlyDayKey, reminder.yearlyDay ?: 0)
            putExtra(ReminderReceiver.hourKey, reminder.hourOfDay)
            putExtra(ReminderReceiver.minuteKey, reminder.minute)
        }
        val requestCode = reminder.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        context.getSystemService(AlarmManager::class.java)
            .setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    fun savedReminders(context: Context): List<ReminderDraft> {
        val preferences = context.getSharedPreferences(savedPreferencesName, Context.MODE_PRIVATE)
        val reminders = linkedMapOf<String, ReminderDraft>()
        preferences.getStringSet(reminderIdsKey, emptySet()).orEmpty().forEach { id ->
            val saved = preferences.getString(id, null) ?: return@forEach
            val json = runCatching { JSONObject(saved) }.getOrNull() ?: return@forEach
            runCatching {
                ReminderDraft(
                    id = id,
                    title = json.getString("title"),
                    message = json.getString("message"),
                    triggerAtMillis = json.optLong("triggerAtMillis", 0L),
                    monthlyDay = json.optInt("monthlyDay").takeIf { it in 1..31 },
                    yearlyMonth = json.optInt("yearlyMonth").takeIf { it in 1..12 },
                    yearlyDay = json.optInt("yearlyDay").takeIf { it in 1..31 },
                    hourOfDay = json.optInt("hour", 9),
                    minute = json.optInt("minute", 0)
                )
            }.getOrNull()?.let { reminders[id] = it }
        }

        val monthly = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        monthly.getStringSet(reminderIdsKey, emptySet()).orEmpty().forEach { id ->
            if (id in reminders) return@forEach
            val fields = monthly.getString(id, null)?.split("\u001f") ?: return@forEach
            if (fields.size != 4) return@forEach
            val time = fields[3].split(":")
            reminders[id] = ReminderDraft(
                id = id,
                title = fields[0],
                message = fields[1],
                triggerAtMillis = 0L,
                monthlyDay = fields[2].toIntOrNull()?.takeIf { it in 1..31 } ?: return@forEach,
                hourOfDay = time.getOrNull(0)?.toIntOrNull() ?: 9,
                minute = time.getOrNull(1)?.toIntOrNull() ?: 0
            )
        }

        val yearly = context.getSharedPreferences(yearlyPreferencesName, Context.MODE_PRIVATE)
        yearly.getStringSet(reminderIdsKey, emptySet()).orEmpty().forEach { id ->
            if (id in reminders) return@forEach
            val fields = yearly.getString(id, null)?.split("\u001f") ?: return@forEach
            if (fields.size != 6) return@forEach
            reminders[id] = ReminderDraft(
                id = id,
                title = fields[0],
                message = fields[1],
                triggerAtMillis = 0L,
                yearlyMonth = fields[2].toIntOrNull()?.takeIf { it in 1..12 } ?: return@forEach,
                yearlyDay = fields[3].toIntOrNull()?.takeIf { it in 1..31 } ?: return@forEach,
                hourOfDay = fields[4].toIntOrNull() ?: 9,
                minute = fields[5].toIntOrNull() ?: 0
            )
        }
        return reminders.values.sortedBy {
            if (it.monthlyDay != null || it.yearlyMonth != null) Long.MAX_VALUE else it.triggerAtMillis
        }
    }

    fun cancel(context: Context, id: String) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val alarmIntent = Intent(context, ReminderReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            alarmIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )?.let { pendingIntent ->
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        context.getSystemService(android.app.NotificationManager::class.java).cancel(id.hashCode())
        removeSavedReminder(context, savedPreferencesName, id)
        removeSavedReminder(context, preferencesName, id)
        removeSavedReminder(context, yearlyPreferencesName, id)
    }

    fun scheduleNextMonthly(
        context: Context,
        title: String,
        message: String,
        id: String,
        day: Int,
        hour: Int,
        minute: Int
    ) {
        schedule(
            context,
            ReminderDraft(
                title = title,
                message = message,
                triggerAtMillis = 0L,
                monthlyDay = day,
                hourOfDay = hour,
                minute = minute,
                id = id
            )
        )
    }

    fun scheduleNextYearly(
        context: Context,
        title: String,
        message: String,
        id: String,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ) {
        schedule(
            context,
            ReminderDraft(
                title = title,
                message = message,
                triggerAtMillis = 0L,
                yearlyMonth = month,
                yearlyDay = day,
                hourOfDay = hour,
                minute = minute,
                id = id
            )
        )
    }

    fun rescheduleMonthlyReminders(context: Context) {
        val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        preferences.getStringSet(reminderIdsKey, emptySet()).orEmpty().forEach { id ->
            val reminder = preferences.getString(id, null)?.split("\u001f") ?: return@forEach
            if (reminder.size != 4) return@forEach
            scheduleNextMonthly(
                context = context,
                title = reminder[0],
                message = reminder[1],
                id = id,
                day = reminder[2].toIntOrNull() ?: return@forEach,
                hour = reminder[3].substringBefore(':').toIntOrNull() ?: return@forEach,
                minute = reminder[3].substringAfter(':').toIntOrNull() ?: return@forEach
            )
        }
    }

    fun rescheduleYearlyReminders(context: Context) {
        val preferences = context.getSharedPreferences(yearlyPreferencesName, Context.MODE_PRIVATE)
        preferences.getStringSet(reminderIdsKey, emptySet()).orEmpty().forEach { id ->
            val reminder = preferences.getString(id, null)?.split("\u001f") ?: return@forEach
            if (reminder.size != 6) return@forEach
            schedule(
                context,
                ReminderDraft(
                    title = reminder[0],
                    message = reminder[1],
                    triggerAtMillis = 0L,
                    yearlyMonth = reminder[2].toIntOrNull() ?: return@forEach,
                    yearlyDay = reminder[3].toIntOrNull() ?: return@forEach,
                    hourOfDay = reminder[4].toIntOrNull() ?: return@forEach,
                    minute = reminder[5].toIntOrNull() ?: return@forEach,
                    id = id
                )
            )
        }
    }

    private fun saveMonthlyReminder(context: Context, reminder: ReminderDraft, day: Int) {
        val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        val ids = preferences.getStringSet(reminderIdsKey, emptySet()).orEmpty().toMutableSet()
        ids += reminder.id
        preferences.edit()
            .putStringSet(reminderIdsKey, ids)
            .putString(
                reminder.id,
                listOf(
                    reminder.title,
                    reminder.message,
                    day.toString(),
                    "${reminder.hourOfDay}:${reminder.minute}"
                ).joinToString("\u001f")
            )
            .apply()
    }

    private fun saveReminder(context: Context, reminder: ReminderDraft) {
        val preferences = context.getSharedPreferences(savedPreferencesName, Context.MODE_PRIVATE)
        val ids = preferences.getStringSet(reminderIdsKey, emptySet()).orEmpty().toMutableSet()
        ids += reminder.id
        val json = JSONObject()
            .put("title", reminder.title)
            .put("message", reminder.message)
            .put("triggerAtMillis", reminder.triggerAtMillis)
            .put("monthlyDay", reminder.monthlyDay ?: 0)
            .put("yearlyMonth", reminder.yearlyMonth ?: 0)
            .put("yearlyDay", reminder.yearlyDay ?: 0)
            .put("hour", reminder.hourOfDay)
            .put("minute", reminder.minute)
        preferences.edit().putStringSet(reminderIdsKey, ids).putString(reminder.id, json.toString()).apply()
    }

    private fun removeSavedReminder(context: Context, preferencesName: String, id: String) {
        val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
        val ids = preferences.getStringSet(reminderIdsKey, emptySet()).orEmpty().toMutableSet()
        ids.remove(id)
        preferences.edit().putStringSet(reminderIdsKey, ids).remove(id).apply()
    }

    private fun saveYearlyReminder(context: Context, reminder: ReminderDraft, month: Int, day: Int) {
        val preferences = context.getSharedPreferences(yearlyPreferencesName, Context.MODE_PRIVATE)
        val ids = preferences.getStringSet(reminderIdsKey, emptySet()).orEmpty().toMutableSet()
        ids += reminder.id
        preferences.edit()
            .putStringSet(reminderIdsKey, ids)
            .putString(
                reminder.id,
                listOf(
                    reminder.title,
                    reminder.message,
                    month.toString(),
                    day.toString(),
                    reminder.hourOfDay.toString(),
                    reminder.minute.toString()
                ).joinToString("\u001f")
            )
            .apply()
    }

    private fun nextMonthlyTrigger(day: Int, hour: Int, minute: Int, afterMillis: Long): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.ofInstant(Instant.ofEpochMilli(afterMillis), zone)
        for (monthOffset in 0..2) {
            val month = YearMonth.from(now).plusMonths(monthOffset.toLong())
            val scheduledDate = month.atDay(day.coerceIn(1, month.lengthOfMonth()))
            val candidate = scheduledDate.atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            if (candidate.isAfter(now)) {
                return candidate.atZone(zone).toInstant().toEpochMilli()
            }
        }
        return now.plusMonths(1).atZone(zone).toInstant().toEpochMilli()
    }

    private fun nextYearlyTrigger(
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        afterMillis: Long
    ): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.ofInstant(Instant.ofEpochMilli(afterMillis), zone)
        val selectedMonth = Month.of(month.coerceIn(1, 12))
        for (yearOffset in 0..2) {
            val yearMonth = YearMonth.of(now.year + yearOffset, selectedMonth)
            val date = yearMonth.atDay(day.coerceIn(1, yearMonth.lengthOfMonth()))
            val candidate = date.atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            if (candidate.isAfter(now)) {
                return candidate.atZone(zone).toInstant().toEpochMilli()
            }
        }
        return now.plusYears(1).atZone(zone).toInstant().toEpochMilli()
    }
}
