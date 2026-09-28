package com.example.data.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

class NetworkMonitor(private val context: Context) {

  val isOnline: Flow<Boolean> = callbackFlow {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    if (connectivityManager == null) {
      trySend(false)
      close()
      return@callbackFlow
    }

    val callback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        trySend(true)
      }

      override fun onLost(network: Network) {
        trySend(checkCurrentConnectivity(connectivityManager))
      }

      override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        trySend(hasInternet)
      }
    }

    // Emit initial status
    trySend(checkCurrentConnectivity(connectivityManager))

    val request = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()
    connectivityManager.registerNetworkCallback(request, callback)

    awaitClose {
      connectivityManager.unregisterNetworkCallback(callback)
    }
  }.conflate()

  fun isConnected(): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    return checkCurrentConnectivity(connectivityManager)
  }

  private fun checkCurrentConnectivity(connectivityManager: ConnectivityManager): Boolean {
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }
}
