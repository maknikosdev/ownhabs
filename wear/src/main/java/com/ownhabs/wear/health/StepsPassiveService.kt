package com.ownhabs.wear.health

import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val PATH_STEPS = "/ownhabs/steps"

/**
 * Τρέχει στο παρασκήνιο στο ρολόι· το Health Services API "ξυπνά" αυτή την υπηρεσία κάθε
 * φορά που έχει νέο σωρευτικό σύνολο βημάτων ημέρας να αναφέρει (DataType.STEPS_DAILY),
 * ακόμα κι όταν η εφαρμογή δεν είναι ανοιχτή. Απλά προωθούμε τον αριθμό στο τηλέφωνο μέσω
 * του Data Layer API — Bluetooth, καμία σύνδεση internet/cloud. Αν δεν υπάρχει συζευγμένο
 * τηλέφωνο αυτή τη στιγμή, το putDataItem απλά αποθηκεύεται και παραδίδεται μόλις συνδεθεί.
 */
class StepsPassiveService : PassiveListenerService() {

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        // DataType.STEPS_DAILY είναι AggregateDataType -> ένα μόνο CumulativeDataPoint,
        // με "total" = βήματα από τα μεσάνυχτα (τοπική ώρα ρολογιού) μέχρι τώρα.
        val cumulative = dataPoints.getData(DataType.STEPS_DAILY) ?: return
        val steps = cumulative.total.toInt()
        if (steps < 0) return

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val request = PutDataMapRequest.create(PATH_STEPS).apply {
                    dataMap.putInt("steps", steps)
                    dataMap.putLong("updatedAt", System.currentTimeMillis())
                }.asPutDataRequest().setUrgent()
                Wearable.getDataClient(applicationContext).putDataItem(request)
            }
            // Αν δεν υπάρχει Google Play Services διαθέσιμο, αγνοούμε σιωπηλά —
            // η λειτουργία βημάτων είναι προαιρετική, ποτέ δεν μπλοκάρει τίποτα άλλο.
        }
    }
}
