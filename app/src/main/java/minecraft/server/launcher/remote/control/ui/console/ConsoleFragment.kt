package minecraft.server.launcher.remote.control.ui.console

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import minecraft.server.launcher.remote.control.ConsoleRecyclerviewAdapter
import minecraft.server.launcher.remote.control.MainActivity
import minecraft.server.launcher.remote.control.MslClient
import minecraft.server.launcher.remote.control.databinding.FragmentConsoleBinding
import org.json.JSONArray

class ConsoleFragment : Fragment() {

    private var _binding: FragmentConsoleBinding? = null
    private val binding get() = _binding!!

    private var isScrolledToBottom = true

    private lateinit var mslClient: MslClient
    private lateinit var consoleRecyclerView: RecyclerView
    private lateinit var adapter: ConsoleRecyclerviewAdapter
    private lateinit var mainActivity: MainActivity
    private var serverIsRunning = false
    private var lastKnownLineNumber = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(ConsoleViewModel::class.java)

        _binding = FragmentConsoleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications
        notificationsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        consoleRecyclerView = binding.consoleRecyclerview
        consoleRecyclerView.itemAnimator = null
        consoleRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ConsoleRecyclerviewAdapter()
        consoleRecyclerView.adapter = adapter

        mainActivity = (activity as? MainActivity)!!
        mslClient = mainActivity.mslClient

        mainActivity.serverIsRunning.observe(viewLifecycleOwner) { isRunning ->
            if (isRunning && !serverIsRunning) {
                serverIsRunning = true
                lifecycleScope.launch(Dispatchers.IO) {
                    runConsoleCoroutine()
                }
            } else if (!isRunning) {
                adapter.clear()
                serverIsRunning = false
                lastKnownLineNumber = 0
            }
        }

        return root
    }

    private fun runConsoleCoroutine() {
        CoroutineScope(Dispatchers.IO).launch {
            while (serverIsRunning) {
                val consoleLines = mslClient.getConsoleLog(lastKnownLineNumber)
                if (consoleLines != null) {
                    val jsonArray = JSONArray(consoleLines)

                    val lines = mutableListOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        val line = jsonArray.getString(i)
                        lines.add(line)
                    }
                    lastKnownLineNumber += lines.size

                    CoroutineScope(Dispatchers.Main).launch {
                        val layoutManager = consoleRecyclerView.layoutManager as LinearLayoutManager
                        for (line in lines) {
                            val itemCount = adapter.itemCount
                            val visibleItem = layoutManager.findLastVisibleItemPosition()
                            val diff = (itemCount - visibleItem)
                            isScrolledToBottom =
                                (diff < 5) && (consoleRecyclerView.scrollState == SCROLL_STATE_IDLE)
                            adapter.addData(line)
                            if (isScrolledToBottom) {
                                consoleRecyclerView.scrollToPosition(adapter.itemCount - 1)
                            }
                        }
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
