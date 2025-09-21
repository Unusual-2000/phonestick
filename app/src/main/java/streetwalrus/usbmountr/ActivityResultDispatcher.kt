@@ -29,2 +29,7 @@
-        mHandlers[mCurId] = handler
-        return mCurId++
+        try {
+            mHandlers[mCurId] = handler
+            return mCurId++
+        } catch (e: Exception) {
+            Log.e(TAG, "Error registering handler: ${e.message}", e)
+            return -1
+        }
@@ -34 +39,5 @@
-        mHandlers.remove(id)
+        try {
+            mHandlers.remove(id)
+        } catch (e: Exception) {
+            Log.e(TAG, "Error removing handler: ${e.message}", e)
+        }
