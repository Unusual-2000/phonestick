package streetwalrus.usbmountr;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import android.text.TextUtils;

public class PathResolver {
    public static String getPath(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    // Below logic is how External Storage provider build URI for documents
                    // Based on http://stackoverflow.com/questions/28605278/android-5-sd-card-label and https://gist.github.com/prasad321/9852037
                    StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

                    try {
                        Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
                        Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
                        Method getUuid = storageVolumeClazz.getMethod("getUuid");
                        Method getState = storageVolumeClazz.getMethod("getState");
                        Method getPath = storageVolumeClazz.getMethod("getPath");
                        Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
                        Method isEmulated = storageVolumeClazz.getMethod("isEmulated");

                        Object result = getVolumeList.invoke(mStorageManager);

                        final int length = Array.getLength(result);
                        for (int i = 0; i < length; i++) {
                            Object storageVolumeElement = Array.get(result, i);
                            //String uuid = (String) getUuid.invoke(storageVolumeElement);

                            final boolean mounted = Environment.MEDIA_MOUNTED.equals(getState.invoke(storageVolumeElement))
                                    || Environment.MEDIA_MOUNTED_READ_ONLY.equals(getState.invoke(storageVolumeElement));

                            //if the media is not mounted, we need not get the volume details
                            if (!mounted) continue;

                            //Primary storage is already handled.
                            if ((Boolean)isPrimary.invoke(storageVolumeElement) && (Boolean)isEmulated.invoke(storageVolumeElement)) continue;

                            String uuid = (String) getUuid.invoke(storageVolumeElement);

                            if (uuid != null && uuid.equals(type))
                            {
                                String res =getPath.invoke(storageVolumeElement) + "/" +split[1];
                                return res;
                            }
                        }
                    }
                    catch (Exception ex) {
                        Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);

                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    try {
                        final Uri contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                        return getDataColumn(context, contentUri, null, null);
                    } catch (NumberFormatException e) {
                         return null;
                    }
                }
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
-        // MediaStore (and general)
-        else if ("content".equalsIgnoreCase(uri.getScheme())) {
-            // Return the remote address
-            if (isGooglePhotosUri(uri))
-                return uri.getLastPathSegment();
-            return getDataColumn(context, uri, null, null);
+            // MediaStore (and general)
+            else if ("content".equalsIgnoreCase(uri.getScheme())) {
+                try {
+                    // Return the remote address
+                    if (isGooglePhotosUri(uri))
+                        return uri.getLastPathSegment();
+                    return getDataColumn(context, uri, null, null);
+                } catch (Exception e) {
+                    android.util.Log.e(TAG, "Error processing content URI: " + e.getMessage(), e);
+                    return null;
+                }
+            }
+            // File
+            else if ("file".equalsIgnoreCase(uri.getScheme())) {
+                try {
+                    return uri.getPath();
+                } catch (Exception e) {
+                    android.util.Log.e(TAG, "Error processing file URI: " + e.getMessage(), e);
+                    return null;
+                }
+            }
+            return null;
+        } catch (Exception e) {
+            android.util.Log.e(TAG, "Critical error in getPath: " + e.getMessage(), e);
+            Toast.makeText(context, context.getString(R.string.path_resolver_error, e.getMessage()), Toast.LENGTH_LONG).show();
+            return null;
@@ -138,5 +157,0 @@
-        // File
-        else if ("file".equalsIgnoreCase(uri.getScheme())) {
-            return uri.getPath();
