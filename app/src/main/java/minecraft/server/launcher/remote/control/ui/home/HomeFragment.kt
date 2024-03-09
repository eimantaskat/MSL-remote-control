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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import minecraft.server.launcher.remote.control.MainActivity
import minecraft.server.launcher.remote.control.MslClient
import minecraft.server.launcher.remote.control.R
import minecraft.server.launcher.remote.control.databinding.FragmentHomeBinding
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
        homeViewModel.text.observe(viewLifecycleOwner) { newText ->
            binding.statusText.text = newText
        }

        homeViewModel.loadingVisibility.observe(viewLifecycleOwner) { visibility ->
            binding.loadingProgessBar.visibility = visibility
        }

        homeViewModel.infoTextVisibility.observe(viewLifecycleOwner) { visibility ->
            binding.infoText.visibility = visibility
        }

        homeViewModel.notConnectedButtonsVisibility.observe(viewLifecycleOwner) { visibility ->
            binding.retryButton.visibility = visibility
            binding.scanQrButton.visibility = visibility
        }

        homeViewModel.textColor.observe(viewLifecycleOwner) { color ->
            binding.statusText.setTextColor(ContextCompat.getColor(requireContext(), color))
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

    private suspend fun setStateFromCoroutine(state: ConnectionState) {
        withContext(Dispatchers.Main) {
            setState(state)
        }
    }

    private fun setState(state: ConnectionState) {
        val viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        when (state) {
            ConnectionState.CONNECTING -> {
                viewModel.updateText(getString(R.string.status_connecting))
                viewModel.setLoadingVisibility(View.VISIBLE)
                viewModel.setInfoTextVisibility(View.INVISIBLE)
                viewModel.setNotConnectedButtonsVisibility(View.INVISIBLE)

                val nightModeFlags = requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    viewModel.setStatusTextColor(android.R.color.white)
                } else {
                    viewModel.setStatusTextColor(android.R.color.black)
                }
            }
            ConnectionState.CONNECTED -> {
                viewModel.updateText(getString(R.string.status_connected))
                viewModel.setLoadingVisibility(View.INVISIBLE)
                viewModel.setInfoTextVisibility(View.INVISIBLE)
                viewModel.setNotConnectedButtonsVisibility(View.INVISIBLE)
                viewModel.setStatusTextColor(android.R.color.holo_green_light)
            }
            ConnectionState.NOT_CONNECTED -> {
                viewModel.updateText(getString(R.string.status_not_connected))
                viewModel.setLoadingVisibility(View.INVISIBLE)
                viewModel.setInfoTextVisibility(View.VISIBLE)
                viewModel.setNotConnectedButtonsVisibility(View.VISIBLE)
                viewModel.setStatusTextColor(android.R.color.holo_red_dark)
            }
        }
    }

}