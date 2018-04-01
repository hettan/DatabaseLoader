package com.jacob.databaseloader

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val contentViewModel by lazy {
        ViewModelProviders.of(this).get(ContentViewModel::class.java)
    }

    private lateinit var fileNamesAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fileNamesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item)
        with(file_names) {
            adapter = fileNamesAdapter
            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    contentViewModel.selectedFile = -1
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?,
                                            position: Int, id: Long) {
                    contentViewModel.selectedFile = position
                }
            }
        }

        contentViewModel.loading.observe(this, Observer {
            val loading = it ?: false
            progress.showIf(loading)
            val error = contentViewModel.errorMessage.value != null
            container.showIf(!loading && !error)
            error_text.showIf(!loading && error)
        })

        contentViewModel.fileNames.observe(this, Observer {
            fileNamesAdapter.clear()
            fileNamesAdapter.addAll(it)
        })

        contentViewModel.fileContent.observe(this, Observer {
            file_content.text = it
        })

        contentViewModel.errorMessage.observe(this, Observer {
            error_text.text = it
        })
    }
}

private fun View.showIf(show: Boolean) {
    visibility = if (show == true) View.VISIBLE else View.GONE
}
