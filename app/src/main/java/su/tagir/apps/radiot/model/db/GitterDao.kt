package su.tagir.apps.radiot.model.db

import androidx.paging.DataSource
import androidx.room.*
import su.tagir.apps.radiot.model.entries.GitterMessage
import su.tagir.apps.radiot.model.entries.Message
import su.tagir.apps.radiot.model.entries.User

@Dao
abstract class GitterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertMessage(message: Message?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertUser(user: User?)

    @Query("DELETE FROM ${Message.TABLE}")
    abstract fun deleteMessages()

    @Query("SELECT * FROM ${Message.TABLE} ORDER BY ${Message.SENT} DESC")
    abstract fun getMessages():DataSource.Factory<Int, Message>

    @Query("SELECT * FROM chat_user WHERE id = :id")
    abstract fun getUser(id: String?): User?

    @Transaction
    open fun saveMessages(messages: List<GitterMessage>?) {
        messages?.forEach {
            insertMessage(it.toMessage())
            insertUser(it.fromUser)
        }
    }

    @Transaction
    open fun updateMessages(messages: List<GitterMessage>?){
        deleteMessages()
        messages?.forEach{
            insertMessage(it.toMessage())
            insertUser(it.fromUser)
        }
    }

    @Transaction
    open fun saveMessage(message: GitterMessage?){
        insertMessage(message?.toMessage())
        insertUser(message?.fromUser)
    }



}