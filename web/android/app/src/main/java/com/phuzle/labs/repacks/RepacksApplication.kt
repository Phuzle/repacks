package com.phuzle.labs.repacks

import android.app.Application
import com.phuzle.labs.repacks.core.AppContainer
import com.phuzle.labs.repacks.work.WorkScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RepacksApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notificationHelper.ensureChannel()

        CoroutineScope(Dispatchers.Default).launch {
            val prefs = container.userPreferencesRepository.current()
            WorkScheduler.reschedule(applicationContext, prefs)
        }
    }
}
