))?.isEmpty() ?: true)) {
-                    return R.string.host_disable_success
-                } else {
-                  return R.string.host_noroot
+                        ))?.isEmpty() ?: true)) {
+                            R.string.host_disable_success
+                        } else {
+                            R.string.host_noroot
+                        }
+                    } catch (e: Exception) {
+                        Log.e(TAG, "Error disabling USB gadget: ${e.message}", e)
+                        R.string.host_error_disable
+                    }
@@ -173,0 +179,3 @@
+            } catch (e: Exception) {
+                Log.e(TAG, "Critical error in USB script execution: ${e.message}", e)
+                R.string.host_error_critical
