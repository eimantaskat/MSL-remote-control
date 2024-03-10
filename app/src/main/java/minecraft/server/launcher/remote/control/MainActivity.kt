package minecraft.server.launcher.remote.control

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import minecraft.server.launcher.remote.control.databinding.ActivityMainBinding
import minecraft.server.launcher.remote.control.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {
    var serverIsRunning = MutableLiveData<Boolean>().apply {
        value = false
    }

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainViewModel
    private lateinit var dataStoreManager: DataStoreManager
    lateinit var mslClient: MslClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        dataStoreManager = DataStoreManager(this)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_console
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        mslClient = MslClient(viewModel)
        val fragmentContainer =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
        if (fragmentContainer is NavHostFragment) {
            val homeFragment =
                fragmentContainer.childFragmentManager.fragments.firstOrNull { it is HomeFragment } as? HomeFragment
            homeFragment?.initMslClient(mslClient)
            homeFragment?.setMainActivity(this)
            homeFragment?.updateServerStatus()
        }
    }
}