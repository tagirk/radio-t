package su.tagir.apps.radiot.model.repository

import su.tagir.apps.radiot.model.api.RemarkClient

class CommentsRepositoryImpl(private val remarkClient: RemarkClient) : CommentsRepository {

    override suspend fun getComments(url: String) = remarkClient.getCommentsTree(postUrl = url)


}