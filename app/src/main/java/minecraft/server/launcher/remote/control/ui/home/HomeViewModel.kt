package minecraft.server.launcher.remote.control.ui.home

import android.opengl.Visibility
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _statusText = MutableLiveData<String>()
    val statusText: LiveData<String> = _statusText

    private val _loadingVisibility = MutableLiveData<Int>().apply {
        value = View.INVISIBLE
    }
    val loadingVisibility: LiveData<Int> = _loadingVisibility

    private val _infoTextVisibility = MutableLiveData<Int>().apply {
        value = View.INVISIBLE
    }
    val infoTextVisibility: LiveData<Int> = _infoTextVisibility

    private val _infoText = MutableLiveData<String>()
    val infoText: LiveData<String> = _infoText

    private val _notConnectedButtonsVisibility = MutableLiveData<Int>().apply {
        value = View.INVISIBLE
    }
    val notConnectedButtonsVisibility: LiveData<Int> = _notConnectedButtonsVisibility

    private val _statusTextVisibility = MutableLiveData<Int>().apply {
        value = View.VISIBLE
    }
    val statusTextVisibility: LiveData<Int> = _statusTextVisibility

    private val _statusTextColor = MutableLiveData<Int>()
    val statusTextColor: LiveData<Int> = _statusTextColor

    private val _serverInfoLayoutVisibility = MutableLiveData<Int>().apply {
        value = View.INVISIBLE
    }
    val serverInfoLayoutVisibility: LiveData<Int> = _serverInfoLayoutVisibility

    private val _serverVersionText = MutableLiveData<String>()
    val serverVersionText: LiveData<String> = _serverVersionText

    private val _playerCountText = MutableLiveData<String>()
    val playerCountText: LiveData<String> = _playerCountText

    private val _serverNameText = MutableLiveData<String>()
    val serverNameText: LiveData<String> = _serverNameText

    private val _serverDescriptionText = MutableLiveData<String>()
    val serverDescriptionText: LiveData<String> = _serverDescriptionText

    fun setInfoText(newText: String) {
        _infoText.value = newText
    }

    fun setStatusText(newText: String) {
        _statusText.value = newText
    }

    fun setLoadingVisibility(visibility: Int) {
        _loadingVisibility.value = visibility
    }

    fun setInfoTextVisibility(visibility: Int) {
        _infoTextVisibility.value = visibility
    }

    fun setNotConnectedButtonsVisibility(visibility: Int) {
        _notConnectedButtonsVisibility.value = visibility
    }

    fun setStatusTextVisibility(visibility: Int) {
        _statusTextVisibility.value = visibility
    }

    fun setStatusTextColor(colorResId: Int) {
        _statusTextColor.value = colorResId
    }

    fun setServerInfoLayoutVisibility(visibility: Int) {
        _serverInfoLayoutVisibility.value = visibility
    }

    fun setServerVersionText(version: String) {
        _serverVersionText.value = version
    }

    fun setPlayerCountText(onlinePlayers: Int, maxPlayers: Int) {
        _playerCountText.value = "$onlinePlayers / $maxPlayers"
    }

    fun setServerNameText(text: String) {
        _serverNameText.value = text
    }

    fun setDescriptionText(text: String) {
        _serverDescriptionText.value = text
    }
}
