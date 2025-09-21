-        val file = fileList[position]
-        val size = if(file.isDirectory)
-            0.0
-        else
-            file.length().toDouble() / (1 shl 20)
-        holder.filename.text = file.name
-        holder.fileSize.text = holder.fileSize.context.getString(R.string.image_chooser_filesize_mib, size)
-        if(file.isFile) {
-            holder.view.setOnClickListener {
-                val result = Intent()
-                result.putExtra("path", file.path)
-                activity.setResult(Activity.RESULT_OK, result)
-                activity.finish()
+        try {
+            if (position >= fileList.size) {
+                android.util.Log.w(TAG, "Position $position is out of bounds for fileList size ${fileList.size}")
+                return
@@ -46,0 +38,36 @@
+
+            val file = fileList[position]
+            val size = try {
+                if(file.isDirectory)
+                    0.0
+                else
+                    file.length().toDouble() / (1 shl 20)
+            } catch (e: Exception) {
+                android.util.Log.e(TAG, "Error getting file size for: ${file.path}", e)
+                0.0
+            }
+
+            try {
+                holder.filename.text = file.name
+                holder.fileSize.text = holder.fileSize.context.getString(R.string.image_chooser_filesize_mib, size)
+            } catch (e: Exception) {
+                android.util.Log.e(TAG, "Error setting file info in view holder", e)
+                holder.filename.text = "Error loading file"
+                holder.fileSize.text = "0.0 MiB"
+            }
+
+            if(file.isFile) {
+                holder.view.setOnClickListener {
+                    try {
+                        val result = Intent()
+                        result.putExtra("path", file.path)
+                        activity.setResult(Activity.RESULT_OK, result)
+                        activity.finish()
+                    } catch (e: Exception) {
+                        android.util.Log.e(TAG, "Error handling file selection: ${e.message}", e)
+                        Toast.makeText(activity, activity.getString(R.string.file_error_access, e.message), Toast.LENGTH_SHORT).show()
+                    }
+                }
+            }
+        } catch (e: Exception) {
+            android.util.Log.e(TAG, "Critical error in onBindViewHolder: ${e.message}", e)
