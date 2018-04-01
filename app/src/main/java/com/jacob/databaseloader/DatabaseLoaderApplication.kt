package com.jacob.databaseloader

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class DatabaseLoaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        Realm.setDefaultConfiguration(RealmConfiguration.Builder().apply {
            deleteRealmIfMigrationNeeded()
        }.build())
    }
}