package com.aion.mobile

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class QRScannerActivity : Activity() {

    private lateinit var barcodeView: DecoratedBarcodeView
    private var isTorchOn = false
    private var hasResult = false

    private val callback = BarcodeCallback { result ->
        if (hasResult) return@BarcodeCallback
        hasResult = true
        vibrate()
        val intent = Intent().apply { putExtra("SCAN_RESULT", result.text) }
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        barcodeView = DecoratedBarcodeView(this)
        barcodeView.setStatusText("Escanea el código QR de AionUI")
        barcodeView.decodeContinuous(callback)

        val closeButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setBackgroundColor(Color.argb(140, 0, 0, 0))
            scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            setPadding(12, 12, 12, 12)
            setOnClickListener { finish() }
            contentDescription = "Cerrar"
            val size = dp(44)
            layoutParams = FrameLayout.LayoutParams(size, size).apply {
                gravity = Gravity.TOP or Gravity.START
                topMargin = dp(48)
                leftMargin = dp(16)
            }
        }

        val torchButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_compass)
            setBackgroundColor(Color.argb(140, 0, 0, 0))
            scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            setPadding(12, 12, 12, 12)
            setOnClickListener {
                isTorchOn = !isTorchOn
                try {
                    barcodeView.setTorchOn()
                } catch (_: Exception) { }
                contentDescription = if (isTorchOn) "Apagar linterna" else "Encender linterna"
            }
            contentDescription = "Encender linterna"
            val size = dp(48)
            layoutParams = FrameLayout.LayoutParams(size, size).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(64)
            }
        }

        val root = FrameLayout(this).apply {
            addView(barcodeView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))
            addView(closeButton)
            addView(torchButton)
        }

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
