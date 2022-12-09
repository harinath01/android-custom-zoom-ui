package us.zoom.sdksample.util;

import android.app.DownloadManager;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class FileUtils {
    private static final String TAG = "FileUtils";

    private static final String MIMTYPE_FOLDER = "application/vnd.google-apps.folder";
    private final static Object[][] mimeTypesTable = {
            { ".c", "text/plain"},
            { ".conf", "text/plain"},
            { ".cpp", "text/plain"},
            { ".cxx", "text/plain"},
            { ".php", "text/plain"},
            { ".perl", "text/plain"},
            { ".py", "text/plain"},
            { ".vbs", "text/plain"},
            { ".h", "text/plain"},
            { ".java", "text/plain"},
            { ".s", "text/plain"},
            { ".S", "text/plain"},
            { ".log", "text/plain"},
            { ".prop", "text/plain"},
            { ".rc", "text/plain"},
            { ".xml", "text/plain"},
            { ".sh", "text/plain"},
            { ".bat", "text/plain"},
            { ".cmd", "text/plain"},
            { ".txt", "text/plain"},
            { ".js", "text/plain"},
            //	{ ".js", "application/x-javascript" },
            { ".lrc", "text/plain"},
            { ".ini", "text/plain"},
            { ".inf", "text/plain"},
            { ".properties", "text/plain"},

            { ".htm", "text/html"},
            { ".html", "text/html"},

            { ".ics", "text/calendar"},

            { ".bmp", "image/bmp"},
            { ".bmp", "image/x-ms-bmp"},
            { ".gif", "image/gif"},
            { ".jpeg", "image/jpeg"},
            { ".jpg", "image/jpeg"},
            { ".png", "image/png"},

            { ".3gp", "video/3gpp"},
            { ".asf", "video/x-ms-asf"},
            { ".avi", "video/x-msvideo"},
            { ".m4u", "video/vnd.mpegurl"},
            { ".m4v", "video/x-m4v"},
            { ".mov", "video/quicktime"},
            { ".mp4", "video/mp4"},
            { ".mpe", "video/mpeg"},
            { ".mpeg", "video/mpeg"},
            { ".mpg", "video/mpeg"},
            { ".mpg4", "video/mp4"},
            { ".wmv", "video/x-ms-wmv"},
            { ".rmvb", "video/x-pn-realaudio"},
            { ".mkv", "video/x-matroska"},
            { ".flv", "video/x-flv"},

            { ".m3u", "audio/x-mpegurl"},
            { ".m4a", "audio/mp4a-latm"},
            { ".m4b", "audio/mp4a-latm"},
            { ".m4p", "audio/mp4a-latm"},
            { ".mp2", "audio/x-mpeg"},
            { ".mp3", "audio/x-mpeg"},
            { ".mpga", "audio/mpeg"},
            { ".ogg", "audio/ogg"},
            { ".wav", "audio/x-wav"},
            { ".wma", "audio/x-ms-wma"},

            { ".doc", "application/msword"},
            { ".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            { ".xls", "application/vnd.ms-excel"},
            { ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            { ".msg", "application/vnd.ms-outlook"},
            { ".pdf", "application/pdf"},
            { ".pps", "application/vnd.ms-powerpoint"},
            { ".ppt", "application/vnd.ms-powerpoint"},
            { ".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            { ".rtf", "application/rtf"},
            { ".wps", "application/vnd.ms-works"},
            { ".epub", "application/epub+zip"},
            { ".odp", "application/vnd.oasis.opendocument.presentation"},
            { ".ods", "application/vnd.oasis.opendocument.spreadsheet"},
            { ".odt", "application/vnd.oasis.opendocument.text"},

            { ".gtar", "application/x-gtar"},
            { ".gz", "application/x-gzip"},
            { ".jar", "application/java-archive"},
            { ".tar", "application/x-tar"},
            { ".tgz", "application/x-compressed"},
            { ".z", "application/x-compress"},
            { ".zip", "application/x-zip-compressed"},
            {".rar","application/x-rar-compressed"}
    };

    @WorkerThread
    @Nullable
    public static String getReadablePathFromUri(Context context, Uri uri) {
        String path = null;
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            path = uri.getPath();
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            path = getPath(context, uri);
        }

        if (TextUtils.isEmpty(path)) {
            path = copyFiletoCache(context, uri, context.getCacheDir().getAbsolutePath());
        }

        if (TextUtils.isEmpty(path)) {
            return path;
        }

        Log.d(TAG, "get path from uri: " + path);
        if (!isReadablePath(path)) {
            int index = path.lastIndexOf("/");
            String name = path.substring(index + 1);
            String dstPath = context.getCacheDir().getAbsolutePath() + File.separator + name;
            if (copyFile(context, uri, dstPath)) {
                path = dstPath;
                Log.d(TAG, "copy file success: " + path);
            } else {
                Log.d(TAG, "copy file fail!");
            }
        }
        return path;
    }

    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                 String id = DocumentsContract.getDocumentId(uri);
                if (!TextUtils.isEmpty(id)) {
                    if(id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }else if(id.startsWith("msf:")) {
                        id = id.replaceFirst("msf:", "");
                    }
                }

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    if (downloadManager == null) {
                        return null;
                    }

                    final Uri contentUri = downloadManager.getUriForDownloadedFile(Long.valueOf(id));
                    if (contentUri == null) {
                        return null;
                    }
                    return getDataColumn(context, contentUri, null, null);
                }else {
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/my_downloads"),
                            Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = uri;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isReadablePath(@Nullable String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        boolean isLocalPath;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!TextUtils.isEmpty(path)) {
                File localFile = new File(path);
                isLocalPath = localFile.exists() && localFile.canRead();
            } else {
                isLocalPath = false;
            }
        } else {
            isLocalPath = path.startsWith(File.separator);
        }
        return isLocalPath;
    }

    private static boolean copyFile(Context context, Uri uri, String dstPath) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            outputStream = new FileOutputStream(dstPath);

            byte[] buff = new byte[100 * 1024];
            int len;
            while ((len = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    private static String copyFiletoCache(final Context context, @NonNull final Uri uri, String dir) {
        String path = null;

        String tmpFilePath;
        String lastPathSegment = uri.getLastPathSegment();
        if (!TextUtils.isEmpty(lastPathSegment) && isValidFileType(lastPathSegment)) {
            String dataPath = dir;
            if (TextUtils.isEmpty(dir)) {
                dataPath = getDataPath(context);
            } else {
                File fDir = new File(dir);
                if (!fDir.exists())
                    fDir.mkdirs();
            }
            tmpFilePath = dataPath + "/" + lastPathSegment.replace("/", "_");
        } else {
            String type = context.getContentResolver().getType(uri);
            String ext = getFileExtendNameFromMimType(type);
            tmpFilePath = createTempFile(context, "tmp", dir, ext);
        }

        try (InputStream is = context.getContentResolver().openInputStream(uri);
             OutputStream os = new FileOutputStream(tmpFilePath)) {
            do {
                byte[] buff = new byte[100 * 1024];
                @SuppressWarnings("ConstantConditions")
                int size = is.read(buff);
                if (size <= 0)
                    break;

                if (Thread.interrupted()){
                    tmpFilePath = null;
                    break;
                }

                os.write(buff, 0, size);
            } while (true);
            path = tmpFilePath;

        } catch (Exception e) {
        }
        return path;
    }

    private static boolean isValidFileType(@NonNull String filename){
        for(Object[] item : mimeTypesTable){
            String postfix = (String)item[0];
            if(filename.toLowerCase().endsWith(postfix)){
                return true;
            }
        }

        return false;
    }
    
    private static String getDataPath(Context context) {
        if (context == null) {
            return null;
        }
        StringBuilder fn = new StringBuilder();
        File filesDir = context.getFilesDir();
        if(filesDir != null) {
            fn.append(filesDir.getParent());
        } else {
            fn.append("/data/data/" + context.getPackageName());
        }
        fn.append("/data");
        String path = fn.toString();

        File dir = new File(path);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        return path;
    }

    private static String getFileExtendNameFromMimType(String mimType){
        if(TextUtils.isEmpty(mimType)){
            return "";
        }

        if(MIMTYPE_FOLDER.equals(mimType)){
            return "";
        }

        for(Object[] item : mimeTypesTable){
            String sType = (String)item[1];
            if(mimType.equals(sType)){
                return (String)item[0];
            }
        }

        return "";
    }

    private static String createTempFile(Context context, String prefix, String dir, String ext) {
        if(dir == null || dir.length() == 0){
            dir = getDataPath(context);
        } else {
            File fDir = new File(dir);
            if(!fDir.exists())
                fDir.mkdirs();
        }

        if(TextUtils.isEmpty(ext))
            ext = "tmp";

        StringBuilder file = new StringBuilder().append(dir)
                .append("/")
                .append(prefix)
                .append("-")
                .append(UUID.randomUUID().toString());

        if(!ext.startsWith("."))
            file.append(".");

        file.append(ext);

        return file.toString();
    }
}
