package minecraft.server.launcher.remote.control

import android.content.Context
import kotlinx.coroutines.flow.take
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class MslClient(private val viewModel: MainViewModel) {
    private lateinit var privateIp: String
    private lateinit var publicIp: String
    private lateinit var port: String
    private lateinit var password: String

    private fun generateInsecureOkHttpClient(): OkHttpClient {
        // Create a simple builder for our http client
        val httpClientBuilder = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)

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



    fun getServerStatus(context: Context) {
        // Generate a secure OkHttpClient using the provided context
        val httpClient = generateInsecureOkHttpClient()

        val address = "https://$privateIp:$port/is_alive"
        val url = URL(address)

        httpClient.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) {
                println("Error: ${response.code}")
                return
            }

            val responseBody = response.body
            responseBody?.let {
                val inputStream = it.byteStream()
                val reader = BufferedReader(InputStreamReader(inputStream))
                var line: String? = reader.readLine()
                while (line != null) {
                    println(line) // We get a response
                    line = reader.readLine()
                }
                reader.close()
            }
        }

        // TODO: public ip
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