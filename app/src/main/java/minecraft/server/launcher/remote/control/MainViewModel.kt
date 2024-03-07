package minecraft.server.launcher.remote.control

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application): AndroidViewModel(application) {
    val dataStore = DataStoreManager(application)

    val getServerLogin = dataStore.getServerLogin()

    fun setServerInfo(privateIp: String, publicIp: String, port: String, password: String) {
        viewModelScope.launch {
            dataStore.setLoginInfo(privateIp, publicIp, port, password)
        }
    }
}