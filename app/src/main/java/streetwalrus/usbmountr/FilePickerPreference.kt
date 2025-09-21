package streetwalrus.usbmountr

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.preference.Preference
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import java.io.File

class FilePickerPreference : Preference, ActivityResultDispatcher.ActivityResultHandler {
    val TAG = "FilePickerPreference"

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
        : super(context, attrs, defStyleAttr)

    val appContext = context.applicationContext as UsbMountrApplication
    private val mActivityResultId = appContext.mActivityResultDispatcher.registerHandler(this)

    override fun onCreateView(parent: ViewGroup?): View {
        updateSummary()
        return super.onCreateView(parent)
    }
    override fun onPrepareForRemoval() {
        super.onPrepareForRemoval()

        val appContext = context.applicationContext as UsbMountrApplication
        appContext.mActivityResultDispatcher.removeHandler(mActivityResultId)
    }

    override fun onClick() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
-        val value = getPersistedString("")
-        if (value.equals("")) {
+        try {
+            val value = getPersistedString("")
+            if (value.equals("")) {
+                summary = context.getString(R.string.file_picker_nofile)
+            } else {
+                try {
+                    summary = File(value).name
+                } catch (e: Exception) {
+                    Log.e(TAG, "Error getting file name from path: $value", e)
+                    summary = value // Fallback to showing the full path
+                }
+            }
+        } catch (e: Exception) {
+            Log.e(TAG, "Error updating summary: ${e.message}", e)
@@ -97,2 +108,0 @@
-        } else {
-            summary = File(value).name
