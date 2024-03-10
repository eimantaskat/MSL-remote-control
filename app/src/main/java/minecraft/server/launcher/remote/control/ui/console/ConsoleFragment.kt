package minecraft.server.launcher.remote.control.ui.console

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import minecraft.server.launcher.remote.control.ConsoleRecyclerviewAdapter
import minecraft.server.launcher.remote.control.databinding.FragmentConsoleBinding

class ConsoleFragment : Fragment() {

    private var _binding: FragmentConsoleBinding? = null
    private val binding get() = _binding!!

    private var isScrolledToBottom = true

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

        val consoleRecyclerView = binding.consoleRecyclerview
        consoleRecyclerView.itemAnimator = null
        consoleRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = ConsoleRecyclerviewAdapter()
        consoleRecyclerView.adapter = adapter

        CoroutineScope(Dispatchers.Main).launch {
            val layoutManager = consoleRecyclerView.layoutManager as LinearLayoutManager
            for (i in 1..1000) {
                val itemCount = adapter.itemCount
                val visibleItem = layoutManager.findLastVisibleItemPosition()
                val diff = (itemCount - visibleItem)
                isScrolledToBottom =
                    (diff < 5) && (consoleRecyclerView.scrollState == SCROLL_STATE_IDLE)
                adapter.addData("Item $i")
                if (isScrolledToBottom) {
                    consoleRecyclerView.scrollToPosition(adapter.itemCount - 1)
                }
                delay(100)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
