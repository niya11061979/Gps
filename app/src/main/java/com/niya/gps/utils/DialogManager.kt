package com.niya.gps.utils

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import com.niya.gps.R

object DialogManager {
    fun showDialog(context: Context, listener: GeoEnableListener) {
        val builder = AlertDialog.Builder(context)
        val dialog = builder.create()
        with(dialog) {
            setTitle(R.string.dialog_title)
            setMessage(context.getString(R.string.dialog_message))
            setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.dialog_yes)) { _, _ ->
                listener.onClick()
            }
            setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.dialog_no)) { _, _ ->
                Toast.makeText(
                    context,
                    "Приложение не будет работать корректно без разрешения!",
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
            show()
        }
    }

    interface GeoEnableListener {
        fun onClick()
    }
}