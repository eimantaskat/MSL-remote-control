package minecraft.server.launcher.remote.control.ui.properties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import minecraft.server.launcher.remote.control.MainActivity
import minecraft.server.launcher.remote.control.MslClient
import minecraft.server.launcher.remote.control.databinding.FragmentPropertiesBinding
import org.json.JSONObject

class PropertiesFragment : Fragment() {

    private var _binding: FragmentPropertiesBinding? = null

    private lateinit var mainActivity: MainActivity
    private lateinit var mslClient: MslClient
    private lateinit var properties: JSONObject

    private val binding get() = _binding!!
    private lateinit var propertiesViewModel: PropertiesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        propertiesViewModel =
            ViewModelProvider(this)[PropertiesViewModel::class.java]

        _binding = FragmentPropertiesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mainActivity = (activity as? MainActivity)!!
        mslClient = mainActivity.mslClient

        propertiesViewModel.serverInfoLinearLayoutVisibility.observe(viewLifecycleOwner) { visibility ->
            binding.serverInfoScrollView.visibility = visibility
        }


        lifecycleScope.launch(Dispatchers.IO) {
            getServerProperties()
        }

        

//        val textView: TextView = binding.textProperties
//        propertiesViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    private suspend fun getServerProperties() {
        try {
            properties = mslClient.getServerProperties()!!
        } catch (_: Exception) {
            withContext(Dispatchers.Main) {
                propertiesViewModel.setServerInfoLinearLayoutVisibility(View.INVISIBLE)
            }
            return
        }
        withContext(Dispatchers.Main) {
            propertiesViewModel.setServerInfoLinearLayoutVisibility(View.VISIBLE)
        }

        while (!this::properties.isInitialized) {
            delay(100)
        }

        withContext(Dispatchers.Main) {
            val difficultiesArray = arrayOf("Peaceful", "Easy", "Normal", "Hard")
            val difficulty = properties.get("difficulty")
            val difficultyIndex = difficultiesArray.indexOf(difficulty)

            val gamemodeArray = arrayOf("Survival", "Creative", "Adventure", "Spectator")
            val gamemode = properties.get("gamemode")
            val gamemodeIndex = gamemodeArray.indexOf(gamemode)


            binding.difficultyValue.setSelection(difficultyIndex)
            binding.gamemodeValue.setSelection(gamemodeIndex)
            binding.slotsNumberValue.setText(properties.get("max-players").toString())
            binding.whitelistValue.isChecked = properties.get("white-list") as Boolean
            binding.pvpValue.isChecked = properties.get("pvp") as Boolean
            binding.hardcoreValue.isChecked = properties.get("hardcore") as Boolean
            binding.commandBlocksValue.isChecked = properties.get("enable-command-block") as Boolean
            binding.allowFlightValue.isChecked = properties.get("allow-flight") as Boolean
            binding.hideOnlinePlayersValue.isChecked =
                properties.get("hide-online-players") as Boolean
            binding.motdValue.setText(properties.get("motd").toString())
            binding.onlineModeValue.isChecked = properties.get("online-mode") as Boolean
            binding.allowNetherValue.isChecked = properties.get("allow-nether") as Boolean
            binding.forceGamemodeValue.isChecked = properties.get("force-gamemode") as Boolean
            binding.viewDistanceValue.setText(properties.get("view-distance").toString())
            binding.simulationDistanceValue.setText(
                properties.get("simulation-distance").toString()
            )
            binding.spawnProtectionValue.setText(properties.get("spawn-protection").toString())

            binding.difficultyValue.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedDifficulty = difficultiesArray[position]
                        lifecycleScope.launch(Dispatchers.IO) {
                            mslClient.setServerProperties("difficulty", selectedDifficulty)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do nothing if nothing is selected
                    }
                }

            binding.gamemodeValue.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedGamemode = gamemodeArray[position]
                        lifecycleScope.launch(Dispatchers.IO) {
                            mslClient.setServerProperties("gamemode", selectedGamemode)
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do nothing if nothing is selected
                    }
                }

            binding.slotsNumberValue.setOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    val text = (view as EditText).text.toString()
                    lifecycleScope.launch(Dispatchers.IO) {
                        mslClient.setServerProperties("max-players", text)
                    }
                }
            }

            binding.whitelistValue.setOnCheckedChangeListener { _, isChecked ->
                val value = if (isChecked) {
                    "true"
                } else {
                    "false"
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    mslClient.setServerProperties("white-list", value)
                }
            }

            binding.pvpValue.setOnCheckedChangeListener { _, isChecked ->
                val value = if (isChecked) {
                    "true"
                } else {
                    "false"
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    mslClient.setServerProperties("pvp", value)
                }
            }

            binding.hardcoreValue.setOnCheckedChangeListener { _, isChecked ->
                val value = if (isChecked) {
                    "true"
                } else {
                    "false"
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    mslClient.setServerProperties("hardcore", value)
                }
            }

            binding.commandBlocksValue.setOnCheckedChangeListener { _, isChecked ->
                val value = if (isChecked) {
                    "true"
                } else {
                    "false"
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    mslClient.setServerProperties("enable-command-block", value)
                }
            }

            binding.allowFlightValue.setOnCheckedChangeListener { _, isChecked ->
                val value = if (isChecked) {
                    "true"
                } else {
                    "false"
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    mslClient.setServerProperties("allow-flight", value)
                }
            }

            binding.hideOnlinePlayersValue.setOnCheckedChangeListener { _, isChecked ->
                val value = if (isChecked) {
                    "true"
                } else {
                    "false"
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    mslClient.setServerProperties("hide-online-players", value)
                }
            }

            binding.motdValue.setOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    val text = (view as EditText).text.toString()
                    lifecycleScope.launch(Dispatchers.IO) {
                        mslClient.setServerProperties("motd", text)
                    }
                }
            }

            binding.onlineModeValue.setOnCheckedChangeListener { _, isChecked ->
                val value = if (isChecked) {
                    "true"
                } else {
                    "false"
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    mslClient.setServerProperties("online-mode", value)
                }
            }

            binding.allowNetherValue.setOnCheckedChangeListener { _, isChecked ->
                val value = if (isChecked) {
                    "true"
                } else {
                    "false"
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    mslClient.setServerProperties("allow-nether", value)
                }
            }

            binding.forceGamemodeValue.setOnCheckedChangeListener { _, isChecked ->
                val value = if (isChecked) {
                    "true"
                } else {
                    "false"
                }
                lifecycleScope.launch(Dispatchers.IO) {
                    mslClient.setServerProperties("force-gamemode", value)
                }
            }

            binding.viewDistanceValue.setOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    val text = (view as EditText).text.toString()
                    lifecycleScope.launch(Dispatchers.IO) {
                        mslClient.setServerProperties("view-distance", text)
                    }
                }
            }

            binding.simulationDistanceValue.setOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    val text = (view as EditText).text.toString()
                    lifecycleScope.launch(Dispatchers.IO) {
                        mslClient.setServerProperties("simulation-distance", text)
                    }
                }
            }

            binding.spawnProtectionValue.setOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    val text = (view as EditText).text.toString()
                    lifecycleScope.launch(Dispatchers.IO) {
                        mslClient.setServerProperties("spawn-protection", text)
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}