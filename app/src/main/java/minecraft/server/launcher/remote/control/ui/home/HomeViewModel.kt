package minecraft.server.launcher.remote.control.ui.home

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "test"
    }
    val text: LiveData<String> = _text

    private val _loadingVisibility = MutableLiveData<Int>().apply {
        value = View.INVISIBLE
    }
    val loadingVisibility: LiveData<Int> = _loadingVisibility

    private val _infoTextVisibility = MutableLiveData<Int>().apply {
        value = View.INVISIBLE
    }
    val infoTextVisibility: LiveData<Int> = _infoTextVisibility

    private val _notConnectedButtonsVisibility = MutableLiveData<Int>().apply {
        value = View.INVISIBLE
    }
    val notConnectedButtonsVisibility: LiveData<Int> = _notConnectedButtonsVisibility

    private val _statusTextColor = MutableLiveData<Int>()
    val textColor: LiveData<Int> = _statusTextColor

    fun updateText(newText: String) {
        _text.value = newText
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

    fun setStatusTextColor(colorResId: Int) {
        _statusTextColor.value = colorResId
    }
}