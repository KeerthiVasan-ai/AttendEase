package com.keerthi77459.attendease.viewmodel

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.keerthi77459.attendease.R

class AlertDialogBox(context: Context) {

    lateinit var dialog : AlertDialog
    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    private val v: View = LayoutInflater.from(context).inflate(R.layout.fragement_alertbox,null)

    fun displayDialog(message:String) : AlertDialog{
        val displayView : TextView = v.findViewById(R.id.alertbox)
        displayView.text = message
        builder.setView(v)
        builder.setTitle("WARNING")
            .setPositiveButton("I ,Understood",DialogInterface.OnClickListener { _, _ ->

            })
        dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }
}