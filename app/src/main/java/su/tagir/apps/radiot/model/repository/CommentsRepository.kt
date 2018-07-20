package su.tagir.apps.radiot.model.repository

import su.tagir.apps.radiot.model.api.RemarkClient
import javax.inject.Inject

class CommentsRepository @Inject constructor(private val remarkClient:RemarkClient) {


    fun getComments(url: String) = remarkClient.getComments(postUrl = url)


}