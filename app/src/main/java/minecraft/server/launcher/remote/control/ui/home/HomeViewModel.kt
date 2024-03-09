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

    fun updateText(newText: String) {
        _text.value = newText
    }

    fun setLoadingVisibility(visibility: Int) {
        _loadingVisibility.value = visibility
    }
}