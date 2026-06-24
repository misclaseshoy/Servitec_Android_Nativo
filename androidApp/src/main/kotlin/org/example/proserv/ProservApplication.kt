package org.example.proserv

import android.app.Application
import org.example.proserv.data.AppContainer
import org.example.proserv.data.AppDataContainer

class ProservApplication : Application() {
    /**
     * Instancia de AppContainer utilizada por el resto de las clases para obtener dependencias.
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer()
    }
}
