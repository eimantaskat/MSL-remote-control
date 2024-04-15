package minecraft.server.launcher.remote.control.ui.properties

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PropertiesViewModel : ViewModel() {

    private val _serverInfoLinearLayoutVisibility = MutableLiveData<Int>().apply {
        value = View.VISIBLE
    }
    val serverInfoLinearLayoutVisibility: LiveData<Int> = _serverInfoLinearLayoutVisibility

    fun setServerInfoLinearLayoutVisibility(visibility: Int) {
        _serverInfoLinearLayoutVisibility.value = visibility
    }
}