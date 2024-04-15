package minecraft.server.launcher.remote.control

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MslClient(private val viewModel: MainViewModel) {
    private lateinit var privateIp: String
    private lateinit var publicIp: String
    private lateinit var port: String
    private lateinit var password: String

    private lateinit var activeIp: String

    private fun generateInsecureOkHttpClient(callTimeout: Long): OkHttpClient {
        // Create a simple builder for our http client
        val httpClientBuilder = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .callTimeout(callTimeout, TimeUnit.SECONDS)
            .addInterceptor(CookieInterceptor(password)) // Add the CookieInterceptor here

        // Create a TrustManager that trusts all hosts
        val trustAllCerts: Array<TrustManager> = arrayOf(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            }
        )

        // Create an SSLContext that trusts all hosts
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        // Finally, set the sslSocketFactory to our builder and build it
        return httpClientBuilder
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true } // Bypass hostname verification
            .build()
    }

    private class CookieInterceptor(private val password: String) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val requestBuilder: Request.Builder = chain.request().newBuilder()
            requestBuilder.addHeader("Cookie", "password=$password")
            return chain.proceed(requestBuilder.build())
        }
    }

    private fun apiCall(address: String, callTimeout: Long = 30): String? {
        val httpClient = generateInsecureOkHttpClient(callTimeout)
        val url = URL(address)

        return try {
            val response = httpClient.newCall(Request.Builder().url(url).build()).execute()
            if (response.isSuccessful) {
                response.body?.string() // Return the response body as a string
            } else {
                println("Error: ${response.code}")
                null
            }
        } catch (e: Exception) {
            println("An error occurred while making the API call to $address: $e")
            null
        }
    }

    private suspend fun activeApiCall(route: String, callTimeout: Long = 30): String? {
        while (!this::activeIp.isInitialized) {
            delay(100)
        }
        val address = "https://$activeIp:$port/$route"

        val httpClient = generateInsecureOkHttpClient(callTimeout)
        val url = URL(address)

        return try {
            val response = httpClient.newCall(Request.Builder().url(url).build()).execute()
            if (response.isSuccessful) {
                response.body?.string() // Return the response body as a string
            } else {
                println("Error: ${response.code}")
                null
            }
        } catch (e: Exception) {
            println("An error occurred while making the API call to $address: $e")
            null
        }
    }

    private fun apiCallWithUnknownIp(route: String, callTimeout: Long = 30): String? {
        var response = apiCall("https://$privateIp:$port/$route", callTimeout)
        if (response != null) {
            activeIp = privateIp
        } else {
            response = apiCall("https://$publicIp:$port/$route", callTimeout)
            if (response != null) {
                activeIp = publicIp
            }
        }
        return response
    }

    fun getServerStatus(): String? {
        val callTimeout: Long = 5
        if (!this::activeIp.isInitialized) {
            return apiCallWithUnknownIp("get_msl_status", callTimeout)
        }

        return apiCall("https://$activeIp:$port/get_msl_status", callTimeout)
            ?: return apiCallWithUnknownIp("get_msl_status", callTimeout)
    }

    suspend fun getConsoleLog(startLine: Int = 0): String? {
        return activeApiCall("get_console_log?start_line=$startLine")
    }

    suspend fun executeConsoleCommand(command: String): String? {
        val route = "execute_console_command?command=$command"
        return activeApiCall(route)
    }

    suspend fun getServersList(): String {
        return activeApiCall("get_servers_list") ?: ""
    }

    suspend fun startServer(serverName: String): Boolean {
        val route = "start_server?server_name=$serverName"
        val response = activeApiCall(route)
        return response == "OK"
    }

    suspend fun stopServer(): Boolean {
        val response = activeApiCall("stop_server")
        return response == "OK"
    }

    suspend fun getServerProperties(): JSONObject? {
        val response = activeApiCall("get_server_properties") ?: return null
        return JSONObject(response)
    }

    suspend fun setServerProperties(key: String, value: String): Boolean {
        val response = activeApiCall("update_server_properties?key=$key&value=$value")
        return response == "OK"
    }

    suspend fun loadServerInfo() {
        viewModel.dataStore.getServerLogin()
            .take(1) // Limit to the first emitted value
            .collect { serverLoginInfo ->
                privateIp = serverLoginInfo.privateIp
                publicIp = serverLoginInfo.publicIp
                port = serverLoginInfo.port
                password = serverLoginInfo.password
            }
    }

}