package minecraft.server.launcher.remote.control

import android.content.Context
import minecraft.server.launcher.remote.control.MainActivity
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class MslClient(private val viewModel: MainViewModel) {
    private lateinit var privateIp: String
    private lateinit var publicIp: String
    private lateinit var port: String
    private lateinit var password: String

    fun getServerStatus() {
        val url = URL("$privateIp:$port/is_alive")

        with(url.openConnection() as HttpURLConnection) {
            val reader: BufferedReader = inputStream.bufferedReader()
            var line: String? = reader.readLine()
            while (line != null) {
                println(line)
                line = reader.readLine()
            }
            reader.close()
        }
    }

    suspend fun loadServerInfo() {
        viewModel.dataStore.getServerLogin().collect { serverLoginInfo ->
            println(serverLoginInfo)
            // Here you can use serverLoginInfo for further processing
        }
    }

}