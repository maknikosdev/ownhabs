package com.ownhabs.wear

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService

/**
 * Δηλωμένο στο Manifest ώστε το σύστημα να ενημερώνει την εφαρμογή για αλλαγές δεδομένων
 * ακόμα κι όταν δεν είναι ανοιχτή. Το ίδιο το DataItem παραμένει αποθηκευμένο από το Data
 * Layer API αυτόματα — το WearHabitViewModel το διαβάζει απευθείας μόλις ανοίξει η οθόνη,
 * οπότε εδώ δεν χρειάζεται επιπλέον επεξεργασία.
 */
class WatchDataListenerService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        // Σκόπιμα κενό — βλ. σχόλιο πάνω.
    }
}
