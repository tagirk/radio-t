package su.tagir.apps.radiot.model.repository

import su.tagir.apps.radiot.model.api.RemarkClient
import javax.inject.Inject

class CommentsRepositoryImpl @Inject constructor(private val remarkClient:RemarkClient): CommentsRepository {


    override suspend fun getComments(url: String) = remarkClient.getComments(postUrl = url)


}