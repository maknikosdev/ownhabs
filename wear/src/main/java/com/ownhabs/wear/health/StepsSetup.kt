package com.ownhabs.wear.health

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig

/**
 * Καταχωρεί το StepsPassiveService στο Health Services API ώστε να λαμβάνει το σωρευτικό
 * σύνολο βημάτων ημέρας (DataType.STEPS_DAILY) στο παρασκήνιο. Η καταχώρηση είναι φθηνή
 * και ιδεμπότεντη — απλά αντικαθιστά την προηγούμενη — οπότε είναι ασφαλές να καλείται σε
 * κάθε άνοιγμα της εφαρμογής (π.χ. στο onCreate του WearMainActivity), χωρίς να χρειάζεται
 * ξεχωριστό μηχανισμό για επανεκκίνηση συσκευής.
 *
 * Πρέπει να καλείται ΜΟΝΟ αφού έχει δοθεί η άδεια ACTIVITY_RECOGNITION — αλλιώς το Health
 * Services απλά δεν θα στέλνει δεδομένα βημάτων (καμία εξαίρεση, σιωπηλή αποτυχία).
 */
object StepsSetup {
    fun registerPassiveListener(context: Context) {
        runCatching {
            val config = PassiveListenerConfig.builder()
                .setDataTypes(setOf(DataType.STEPS_DAILY))
                .build()
            HealthServices.getClient(context)
                .passiveMonitoringClient
                .setPassiveListenerServiceAsync(StepsPassiveService::class.java, config)
        }
        // Αν η συσκευή δεν υποστηρίζει Health Services (π.χ. emulator χωρίς αισθητήρες),
        // αγνοούμε σιωπηλά — η λειτουργία βημάτων είναι προαιρετική.
    }
}
