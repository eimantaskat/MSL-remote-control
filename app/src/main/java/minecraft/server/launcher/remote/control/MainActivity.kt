package minecraft.server.launcher.remote.control

import android.content.Context
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import minecraft.server.launcher.remote.control.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        loadServerLogin()
    }

    fun saveServerLogin(privateIp: String, publicIp: String, port: String, password: String) {
        val sharedPreferences = getSharedPreferences("serverLogin", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.apply {
            putString("PRIVATE_IP", privateIp)
            putString("PUBLIC_IP", publicIp)
            putString("PORT", port)
            putString("PASSWORD", password)
        }.apply()
    }

    private fun loadServerLogin() {
        val sharedPreferences = getSharedPreferences("serverLogin", Context.MODE_PRIVATE)

        val privateIp = sharedPreferences.getString("PRIVATE_IP", "")
        val publicIp = sharedPreferences.getString("PUBLIC_IP", "")
        val port = sharedPreferences.getString("PORT", "")
        val password = sharedPreferences.getString("PASSWORD", "")

        // TODO: whats next?
    }
}