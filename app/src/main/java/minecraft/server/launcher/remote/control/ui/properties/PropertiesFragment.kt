package minecraft.server.launcher.remote.control.ui.properties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import minecraft.server.launcher.remote.control.databinding.FragmentPropertiesBinding

class PropertiesFragment : Fragment() {

    private var _binding: FragmentPropertiesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val propertiesViewModel =
            ViewModelProvider(this).get(PropertiesViewModel::class.java)

        _binding = FragmentPropertiesBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textProperties
//        propertiesViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}