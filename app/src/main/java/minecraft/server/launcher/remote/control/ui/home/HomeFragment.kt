package minecraft.server.launcher.remote.control.ui.home

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import minecraft.server.launcher.remote.control.MainActivity
import minecraft.server.launcher.remote.control.MslClient
import minecraft.server.launcher.remote.control.R
import minecraft.server.launcher.remote.control.databinding.FragmentHomeBinding
import org.json.JSONObject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


enum class ConnectionState {
    CONNECTING,
    CONNECTED,
    NOT_CONNECTED
}


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private lateinit var mslClient: MslClient

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by activityViewModels()

    private var refreshMslStateJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.readQrButton.setOnClickListener {
            val context = requireContext()
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                showCamera()
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                Toast.makeText(context, R.string.missing_camera_permission_message, Toast.LENGTH_SHORT).show()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }

        // Observe the LiveData from the ViewModel and update UI accordingly
        homeViewModel.statusText.observe(viewLifecycleOwner) { newText ->
            binding.statusText.text = newText
        }

        homeViewModel.loadingVisibility.observe(viewLifecycleOwner) { visibility ->
            binding.loadingProgressBar.visibility = visibility
        }

        homeViewModel.infoTextVisibility.observe(viewLifecycleOwner) { visibility ->
            binding.infoText.visibility = visibility
        }

        homeViewModel.notConnectedButtonsVisibility.observe(viewLifecycleOwner) { visibility ->
            binding.retryButton.visibility = visibility
            binding.scanQrButton.visibility = visibility
        }

        homeViewModel.statusTextColor.observe(viewLifecycleOwner) { color ->
            binding.statusText.setTextColor(ContextCompat.getColor(requireContext(), color))
        }

        homeViewModel.infoText.observe(viewLifecycleOwner) { newText ->
            binding.infoText.text = newText
        }

        homeViewModel.serverInfoLayoutVisibility.observe(viewLifecycleOwner) { visibility ->
            binding.serverInfoLinearLayout.visibility = visibility
        }

        homeViewModel.serverVersionText.observe(viewLifecycleOwner) { version ->
            binding.versionText.text = version
        }

        homeViewModel.playerCountText.observe(viewLifecycleOwner) { playersText ->
            binding.playersText.text = playersText
        }

        homeViewModel.serverNameText.observe(viewLifecycleOwner) { newText ->
            binding.serverNameText.text = newText
        }

        homeViewModel.serverDescriptionText.observe(viewLifecycleOwner) { newText ->
            binding.serverDesriptionText.text = newText
        }

        // Bind functions to buttons
        binding.retryButton.setOnClickListener {
            updateServerStatus()
        }

        binding.scanQrButton.setOnClickListener {
            val context = requireContext()
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                showCamera()
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                Toast.makeText(context, R.string.missing_camera_permission_message, Toast.LENGTH_SHORT).show()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted: Boolean ->
            if (isGranted) {
                showCamera()
            }
            else {
                // TODO: no camera permissions
            }
        }

    @OptIn(ExperimentalEncodingApi::class)
    private val scanLauncher =
        registerForActivityResult(ScanContract()) {
            result: ScanIntentResult ->
                run {
                    if (result.contents == null) {
                        val context = requireContext()
                        Toast.makeText(context, getString(R.string.failed_to_read_qr_code), Toast.LENGTH_SHORT).show()
                    } else {
                        val decodedDataByteArray: ByteArray = Base64.decode(result.contents)
                        val decodedData = String(decodedDataByteArray, Charsets.UTF_8)

                        val parts = decodedData.split(":", limit=4)
                        val privateIp = parts[0]
                        val publicIp = parts[1]
                        val port = parts[2]
                        val password = parts[3]

                        (activity as MainActivity).viewModel.setServerInfo(privateIp, publicIp, port, password)
                        updateServerStatus()
                    }
                }
        }
    private fun showCamera() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt(getString(R.string.scan_qr_code_prompt))
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)

        scanLauncher.launch(options)
    }

    fun initMslClient(newMslClient: MslClient) {
        mslClient = newMslClient
    }

    fun updateServerStatus() {
        lifecycleScope.launch(Dispatchers.IO) {
            setStateFromCoroutine(ConnectionState.CONNECTING)
            mslClient.loadServerInfo()
            val response = mslClient.getServerStatus()
            if (response == null) {
                setStateFromCoroutine(ConnectionState.NOT_CONNECTED)
            } else {
                setStateFromCoroutine(ConnectionState.CONNECTED)
            }
        }
    }

    private fun refreshServerStatus() {
        lifecycleScope.launch(Dispatchers.IO) {
            val response = mslClient.getServerStatus()
            if (response == null) {
                setStateFromCoroutine(ConnectionState.NOT_CONNECTED)
                return@launch
            }

            val jsonResponse = JSONObject(response)
            val isRunning = jsonResponse.getBoolean("is_running")
            if (!isRunning) {
                withContext(Dispatchers.Main) {
                    homeViewModel.setInfoText(getString(R.string.server_not_running))
                    homeViewModel.setInfoTextVisibility(View.VISIBLE)
                    homeViewModel.setServerInfoLayoutVisibility(View.INVISIBLE)
                }
                return@launch
            }

            val info = jsonResponse.getJSONObject("info")
            val serverName = info.getString("name")
            val version = info.getString("version")
            val description = info.getString("description")
            val maxPlayers = info.getInt("max_players")
            val onlinePlayers = info.getInt("online_players")

            withContext(Dispatchers.Main) {
                homeViewModel.setServerNameText(serverName)
                homeViewModel.setDescriptionText(description)
                homeViewModel.setServerVersionText(version)
                homeViewModel.setPlayerCountText(onlinePlayers, maxPlayers)

                homeViewModel.setInfoTextVisibility(View.INVISIBLE)
                homeViewModel.setServerInfoLayoutVisibility(View.VISIBLE)
            }
        }
    }

    private suspend fun setStateFromCoroutine(state: ConnectionState) {
        withContext(Dispatchers.Main) {
            setState(state)
        }
    }

    private fun setState(state: ConnectionState) {
        when (state) {
            ConnectionState.CONNECTING -> {
                homeViewModel.setStatusText(getString(R.string.status_connecting))
                homeViewModel.setLoadingVisibility(View.VISIBLE)
                homeViewModel.setInfoTextVisibility(View.INVISIBLE)
                homeViewModel.setServerInfoLayoutVisibility(View.INVISIBLE)
                homeViewModel.setNotConnectedButtonsVisibility(View.INVISIBLE)

                val nightModeFlags = requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    homeViewModel.setStatusTextColor(android.R.color.white)
                } else {
                    homeViewModel.setStatusTextColor(android.R.color.black)
                }
            }
            ConnectionState.CONNECTED -> {
                homeViewModel.setStatusText(getString(R.string.status_connected))
                homeViewModel.setLoadingVisibility(View.INVISIBLE)
                homeViewModel.setNotConnectedButtonsVisibility(View.INVISIBLE)
                homeViewModel.setStatusTextColor(android.R.color.holo_green_light)

                refreshMslStateJob = CoroutineScope(Dispatchers.Main).launch {
                    while (isActive) {
                        refreshServerStatus()
                        delay(5000)
                    }
                }
            }
            ConnectionState.NOT_CONNECTED -> {
                homeViewModel.setStatusText(getString(R.string.status_not_connected))
                homeViewModel.setLoadingVisibility(View.INVISIBLE)
                homeViewModel.setServerInfoLayoutVisibility(View.INVISIBLE)
                homeViewModel.setInfoText(getString(R.string.home_info_not_connected))
                homeViewModel.setInfoTextVisibility(View.VISIBLE)
                homeViewModel.setNotConnectedButtonsVisibility(View.VISIBLE)
                homeViewModel.setStatusTextColor(android.R.color.holo_red_dark)

                refreshMslStateJob?.cancel()
            }
        }
    }

}