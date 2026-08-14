package com.ownhabs.app.data.backup

import android.content.ContentResolver
import android.net.Uri
import com.ownhabs.app.data.local.entity.FrequencyPeriod
import com.ownhabs.app.data.local.entity.GoalType
import com.ownhabs.app.data.local.entity.HabitEntity
import com.ownhabs.app.data.local.entity.HabitLogEntity
import com.ownhabs.app.data.local.entity.HabitStatus
import com.ownhabs.app.data.local.entity.UnlockedBadgeEntity
import com.ownhabs.app.data.repository.HabitRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException

enum class ImportMode { OVERWRITE, MERGE }

sealed class ImportResult {
    data class Success(val habitsImported: Int, val logsImported: Int) : ImportResult()
    data class InvalidFile(val reason: String) : ImportResult()
}

class BackupManager(
    private val repository: HabitRepository,
    private val contentResolver: ContentResolver
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    /** Reads all 4 tables and writes a pretty-printed JSON document to the given SAF Uri. */
    suspend fun exportTo(uri: Uri): Boolean {
        val backup = OwnHabsBackup(
            exportedAt = System.currentTimeMillis(),
            habits = repository.getAllHabitsOnce().map {
                BackupHabit(
                    id = it.id, title = it.title, description = it.description, icon = it.icon,
                    colorHex = it.colorHex, goalType = it.goalType.name, targetValue = it.targetValue,
                    unit = it.unit, frequencyPeriod = it.frequencyPeriod.name, timesPerPeriod = it.timesPerPeriod,
                    reminderHour = it.reminderHour, reminderMinute = it.reminderMinute,
                    status = it.status.name, createdAt = it.createdAt
                )
            },
            habitLogs = repository.getAllLogsOnce().map {
                BackupHabitLog(it.id, it.habitId, it.timestamp, it.value, it.notes)
            },
            unlockedBadges = repository.getAllUnlockedOnce().map {
                BackupUnlockedBadge(it.id, it.badgeId, it.habitId, it.unlockedAt)
            }
        )

        val text = json.encodeToString(OwnHabsBackup.serializer(), backup)
        return runCatching {
            contentResolver.openOutputStream(uri)?.use { out ->
                out.write(text.toByteArray(Charsets.UTF_8))
            } ?: return false
            true
        }.getOrDefault(false)
    }

    /** Validates the JSON structure, then imports using the chosen mode. */
    suspend fun importFrom(uri: Uri, mode: ImportMode): ImportResult {
        val text = runCatching {
            contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
        }.getOrNull() ?: return ImportResult.InvalidFile("Δεν ήταν δυνατή η ανάγνωση του αρχείου.")

        val backup = try {
            json.decodeFromString(OwnHabsBackup.serializer(), text)
        } catch (e: SerializationException) {
            return ImportResult.InvalidFile("Μη έγκυρη δομή JSON.")
        } catch (e: Exception) {
            return ImportResult.InvalidFile("Άγνωστο σφάλμα ανάγνωσης JSON.")
        }

        // Validation layer
        if (backup.habits.any { it.id.isBlank() || it.title.isBlank() }) {
            return ImportResult.InvalidFile("Μία ή περισσότερες συνήθειες έχουν άκυρα δεδομένα.")
        }

        val habits = backup.habits.map {
            HabitEntity(
                id = it.id, title = it.title, description = it.description, icon = it.icon,
                colorHex = it.colorHex, goalType = GoalType.valueOf(it.goalType), targetValue = it.targetValue,
                unit = it.unit, frequencyPeriod = FrequencyPeriod.valueOf(it.frequencyPeriod), timesPerPeriod = it.timesPerPeriod,
                reminderHour = it.reminderHour, reminderMinute = it.reminderMinute,
                status = HabitStatus.valueOf(it.status), createdAt = it.createdAt
            )
        }
        val logs = backup.habitLogs.map { HabitLogEntity(it.id, it.habitId, it.timestamp, it.value, it.notes) }
        val unlocked = backup.unlockedBadges.map { UnlockedBadgeEntity(it.id, it.badgeId, it.habitId, it.unlockedAt) }

        when (mode) {
            ImportMode.OVERWRITE -> repository.replaceAllData(habits, logs, unlocked)
            ImportMode.MERGE -> repository.mergeData(habits, logs, unlocked)
        }

        return ImportResult.Success(habits.size, logs.size)
    }
}
