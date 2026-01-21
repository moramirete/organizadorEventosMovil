package com.example.organizadoreventosmovil

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.ViewGroup

object LoadingUtils {
    private var dialog: Dialog? = null

    fun showLoading(context: Context) {
        if (dialog?.isShowing == true) return

        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setContentView(R.layout.layout_loading)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            show()
        }
    }

    fun hideLoading() {
        try {
            if (dialog?.isShowing == true) {
                dialog?.dismiss()
            }
        } catch (e: Exception) {
            // Manejo de error silencioso si la activity ya se cerró
        } finally {
            dialog = null
        }
    }
}