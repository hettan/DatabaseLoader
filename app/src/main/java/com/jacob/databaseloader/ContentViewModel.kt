package com.jacob.databaseloader

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.os.AsyncTask
import android.util.Log
import io.realm.Realm
import io.realm.RealmChangeListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream

class ContentViewModel(application: Application) : AndroidViewModel(application) {

    private class JsonFilesAssetsAsyncTask(private val application: Application,
                                           private val realm: Realm,
                                           private val errorCallback: (String) -> Unit)
        : AsyncTask<Unit, Unit, List<String>>() {
        override fun doInBackground(vararg params: Unit?): List<String> {
            // Note: Can't create Realm objects on background thread since it is used on the
            // UI thread.
            val loadedFiles = mutableListOf<String>()
            for (filePath in JSON_FILE_PATHS) {
                var inputStream: InputStream? = null
                var reader: BufferedReader? = null
                try {
                    inputStream = application.assets.open(filePath)
                    reader = inputStream.bufferedReader()

                    // Read json text and pretty format it
                    val json = JSONTokener(reader.readText()).nextValue()
                    // Determine if root is object or array
                    val prettyJson = when(json) {
                        is JSONObject -> json.toString(JSON_INDENT_SPACES)
                        is JSONArray -> json.toString(JSON_INDENT_SPACES)
                        else -> throw JSONException("Invalid json")
                    }

                    loadedFiles.add(prettyJson)
                } catch (e: IOException) {
                    // Should never happen since we know the files are in assets but better safe
                    // than sorry
                    Log.e(TAG, "Error reading file $filePath", e)
                    assert(false)
                    return emptyList()
                } catch (e: JSONException) {
                    // Should never happen since we should've already controlled that the files from
                    // assets are valid json files
                    Log.e(TAG, "Error parsing json for file $filePath", e)
                    assert(false)
                    return emptyList()
                } finally {
                    inputStream?.close()
                    reader?.close()
                }
            }
            return loadedFiles
        }

        override fun onPostExecute(loadedFiles: List<String>) {
            if (loadedFiles.isEmpty()) {
                // Error reading files
                errorCallback(application.getString(R.string.error_reading_files))
                return
            }

            // Add files to Realm
            realm.executeTransaction {
                for (i in 0 until loadedFiles.size) {
                    realm.createObject(RealmJsonFile::class.java).apply {
                        fileName = JSON_FILE_PATHS[i]
                        jsonData = loadedFiles[i]
                    }
                }
                Log.d(TAG, "Added files to Realm")
            }
        }

        override fun onCancelled() {
            errorCallback(application.getString(R.string.unexpected_error))
        }

        companion object {
            private val JSON_FILE_PATHS = arrayOf("colors.json", "batters.json", "widget.json")
            private const val JSON_INDENT_SPACES = 2
        }
    }

    private val _fileNames = MutableLiveData<List<String>>()
    val fileNames: LiveData<List<String>> = _fileNames

    private val _fileContent = MutableLiveData<String>()
    val fileContent: LiveData<String> = _fileContent

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    var selectedFile = -1
        set(value) {
            val jsonFiles = jsonFiles ?: return
            if (value < 0 || value >= jsonFiles.size) {
                // Should never happen
                assert(false)
                return
            }
            field = value
            _fileContent.value = jsonFiles[value].jsonData
        }
    private var jsonFiles: List<RealmJsonFile>? = null

    init {
        _loading.value = true

        Realm.getInstanceAsync(Realm.getDefaultConfiguration()!!, object : Realm.Callback() {
            override fun onSuccess(realm: Realm) {
                realm.where(RealmJsonFile::class.java).findAllAsync()
                        .addChangeListener(RealmChangeListener {
                            if (it.isEmpty()) {
                                Log.d(TAG, "Files not added to Realm yet")
                                JsonFilesAssetsAsyncTask(application, realm) {
                                    _errorMessage.value = it
                                    _loading.value = false
                                }.execute()
                            } else {
                                Log.d(TAG, "Loaded files from Realm: $it")
                                jsonFiles = it
                                _errorMessage.value = null
                                _fileNames.value = it.map { it.fileName }
                                selectedFile = 0
                                _loading.value = false
                            }
                        })
            }
        })
    }

    companion object {
        private const val TAG = "ContentViewModel"
    }
}