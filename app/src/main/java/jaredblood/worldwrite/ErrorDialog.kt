package jaredblood.worldwrite

import android.app.Activity
import android.content.Context
import android.databinding.Observable
import android.databinding.ObservableField
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log


class ErrorDialog(val context: Context): Observable.OnPropertyChangedCallback(){
    override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
        if(sender != null && sender is ObservableField<*> && sender.get() is String) {
            show(sender.get().toString())
        }
    }

    fun show(error: String = "Error"){
        Log.d("errorDialog", "dialog: $error")
        analytics().logEvent("error_main", Bundle().apply {
            putString("message", error)
        })
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Error")
        builder.setMessage(error)
        builder.setPositiveButton("Retry")
        { _, _: Int ->
            (context as Activity).recreate()
        }
        builder.setNegativeButton("Quit")
        { _, _: Int ->
            (context as Activity).finish()
        }
        builder.setOnCancelListener { (context as Activity).finish() }
        builder.create().show()
    }
}