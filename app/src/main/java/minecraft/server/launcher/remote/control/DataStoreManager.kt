package minecraft.server.launcher.remote.control

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ServerLoginInfo(
    val privateIp: String,
    val publicIp: String,
    val port: String,
    val password: String
)

class DataStoreManager(context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("server_login_preferences")
    private val dataStore = context.dataStore

    companion object {
        val privateIpKey = stringPreferencesKey("PRIVATE_IP")
        val publicIpKey = stringPreferencesKey("PUBLIC_IP")
        val portKey = stringPreferencesKey("PORT")
        val passwordKey = stringPreferencesKey("PASSWORD")
    }

    suspend fun setLoginInfo(privateIp: String, publicIp: String, port: String, password: String) {
        dataStore.edit { pref ->
            pref[privateIpKey] = privateIp
            pref[portKey] = port
            pref[passwordKey] = password
        }
    }

    fun getServerLogin(): Flow<ServerLoginInfo> {
        return dataStore.data.map { pref ->
            ServerLoginInfo(
                privateIp = pref[privateIpKey] ?: "",
                publicIp = pref[publicIpKey] ?: "",
                port = pref[portKey] ?: "",
                password = pref[passwordKey] ?: ""
            )
        }
    }
}
