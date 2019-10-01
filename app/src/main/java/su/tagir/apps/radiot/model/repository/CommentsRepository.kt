package su.tagir.apps.radiot.model.repository

import su.tagir.apps.radiot.model.entries.CommentsTree

interface CommentsRepository {

    suspend fun getComments(url: String): CommentsTree
}