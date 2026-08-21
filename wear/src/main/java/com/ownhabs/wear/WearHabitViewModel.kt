package com.ownhabs.wear

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class WearHabit(
    val id: String,
    val title: String,
    val icon: String,
    val colorHex: String,
    val done: Boolean
)

private const val PATH_TODAY_HABITS = "/ownhabs/today_habits"
private const val PATH_TOGGLE_HABIT = "/ownhabs/toggle"

class WearHabitViewModel(application: Application) : AndroidViewModel(application), DataClient.OnDataChangedListener {

    private val _habits = MutableStateFlow<List<WearHabit>>(emptyList())
    val habits: StateFlow<List<WearHabit>> = _habits.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val dataClient by lazy { Wearable.getDataClient(getApplication()) }
    private val messageClient by lazy { Wearable.getMessageClient(getApplication()) }
    private val nodeClient by lazy { Wearable.getNodeClient(getApplication()) }

    init {
        loadInitial()
        dataClient.addListener(this)
    }

    override fun onCleared() {
        dataClient.removeListener(this)
        super.onCleared()
    }

    private fun loadInitial() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val items = dataClient.dataItems.await()
                for (i in 0 until items.count) {
                    val item = items[i]
                    if (item.uri.path == PATH_TODAY_HABITS) {
                        applyDataMap(DataMapItem.fromDataItem(item).dataMap)
                    }
                }
                items.release()
            }
            _loading.value = false
        }
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == PATH_TODAY_HABITS) {
                applyDataMap(DataMapItem.fromDataItem(event.dataItem).dataMap)
            }
        }
        _loading.value = false
    }

    private fun applyDataMap(dataMap: com.google.android.gms.wearable.DataMap) {
        val list = dataMap.getDataMapArrayList("habits") ?: return
        _habits.value = list.map { m ->
            WearHabit(
                id = m.getString("id") ?: "",
                title = m.getString("title") ?: "",
                icon = m.getString("icon") ?: "✅",
                colorHex = m.getString("colorHex") ?: "#2FB6C0",
                done = m.getBoolean("done")
            )
        }
    }

    /** Στέλνει tap-to-complete στο τηλέφωνο. Ενημερώνουμε αισιόδοξα (optimistic) την τοπική
     *  κατάσταση αμέσως, ώστε το ρολόι να νιώθει άμεσο — το τηλέφωνο θα επιβεβαιώσει μετά. */
    fun toggleHabit(habitId: String) {
        _habits.value = _habits.value.map { if (it.id == habitId) it.copy(done = !it.done) else it }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val nodes = nodeClient.connectedNodes.await()
                for (node in nodes) {
                    messageClient.sendMessage(node.id, PATH_TOGGLE_HABIT, habitId.toByteArray(Charsets.UTF_8)).await()
                }
            }
        }
    }
}
