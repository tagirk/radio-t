package su.tagir.apps.radiot.model.repository


interface DownloadManager {

    fun startDownload(url: String?): Long

    fun delete(id: Long): Int

    fun checkDownloadStatus(ids:MutableList<Long>, downloadProgressMap:MutableMap<Long, Int>, fileNames: MutableMap<Long, String>)
}