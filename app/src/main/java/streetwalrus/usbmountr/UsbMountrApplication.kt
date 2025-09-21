package streetwalrus.usbmountr

import android.app.Application
import android.content.Intent
import android.util.Log

class UsbMountrApplication : Application() {
    val mActivityResultDispatcher: ActivityResultDispatcher = ActivityResultDispatcher()

    companion object {
        private const val TAG = "UsbMountrApplication"
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        try {
            mActivityResultDispatcher.onActivityResult(requestCode, resultCode, resultData)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling activity result: ${e.message}", e)
        }
    }
}
