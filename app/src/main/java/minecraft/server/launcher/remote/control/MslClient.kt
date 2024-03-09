package minecraft.server.launcher.remote.control

import kotlinx.coroutines.flow.take
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.lang.Exception
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

    private fun generateInsecureOkHttpClient(): OkHttpClient {
        // Create a simple builder for our http client
        val httpClientBuilder = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .callTimeout(5, TimeUnit.SECONDS)
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

    private fun apiCall(address: String): String? {
        val httpClient = generateInsecureOkHttpClient()
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

    fun getServerStatus(): String? {
        var response = apiCall("https://$privateIp:$port/get_msl_status")
        if (response == null) {
            response = apiCall("https://$publicIp:$port/get_msl_status")
            if (response == null) {
                return null
            }
        }
        return response
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