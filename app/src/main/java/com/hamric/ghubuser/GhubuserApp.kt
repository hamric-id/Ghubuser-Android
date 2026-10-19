package com.hamric.ghubuser

import android.app.Application
import com.hamric.core.database.databaseModule
import com.hamric.core.network.networkModule
import com.hamric.data.dataModule
import com.hamric.ghubuser.di.appModule
import com.hamric.ghubuser.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class GhubuserApp : Application(){
    override fun onCreate() {
        super.onCreate()
        startKoin{
            androidLogger()
            androidContext(this@GhubuserApp)
            modules(
                appModule,
                databaseModule,
                networkModule,
                dataModule,
                viewModelModule
            )
        }
    }
}
