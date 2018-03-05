package su.tagir.apps.radiot.model.repository

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment

class DownloadManagerImpl(private val application: Application) : DownloadManager {

    override fun startDownload(url: String?): Long {
        val uri = Uri.parse(url)
        val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        val request = android.app.DownloadManager.Request(uri)
        request.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI or android.app.DownloadManager.Request.NETWORK_MOBILE)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS, "Radio-T/${uri.lastPathSegment}")
        request.setAllowedOverRoaming(false)
        request.setVisibleInDownloadsUi(true)
        request.allowScanningByMediaScanner()
        return downloadManager.enqueue(request)
    }

    override fun delete(id: Long): Int {
        val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        return downloadManager.remove(id)
    }

    override fun checkDownloadStatus(ids: MutableList<Long>, downloadProgressMap: MutableMap<Long, Int>, fileNames: MutableMap<Long, String>) {
        val query = android.app.DownloadManager.Query()
        query.setFilterById(*ids.toLongArray())
        val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        val cursor = downloadManager.query(query) ?: return
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast) {
                val status = cursor.getInt(cursor.getColumnIndex(android.app.DownloadManager.COLUMN_STATUS))
                val id = cursor.getLong(cursor.getColumnIndex(android.app.DownloadManager.COLUMN_ID))
                when (status) {
                    android.app.DownloadManager.STATUS_FAILED -> {

                    }
                    android.app.DownloadManager.STATUS_PENDING,
                    android.app.DownloadManager.STATUS_PAUSED,
                    android.app.DownloadManager.STATUS_RUNNING -> {
                        val size = cursor.getLong(cursor.getColumnIndex(android.app.DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        val downloaded = cursor.getLong(cursor.getColumnIndex(android.app.DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val progress = ((downloaded * 100) / size).toInt()
                        downloadProgressMap[id] = progress
                        ids.remove(id)
                    }
                    android.app.DownloadManager.STATUS_SUCCESSFUL -> {
                        val fileName = cursor.getString(cursor.getColumnIndex(android.app.DownloadManager.COLUMN_LOCAL_URI))
                        ids.remove(id)
                        fileNames[id] = fileName
                    }
                }
                cursor.moveToNext()
            }
        }
        cursor.close()
    }

}