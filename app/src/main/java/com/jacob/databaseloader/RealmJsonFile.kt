package com.jacob.databaseloader

import io.realm.RealmObject

open class RealmJsonFile : RealmObject() {
    var fileName = ""
    var jsonData = ""
}