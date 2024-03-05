package com.dluvian.voyage.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.dluvian.voyage.data.model.RelayedItem
import com.dluvian.voyage.data.room.entity.PostEntity
import com.dluvian.voyage.data.room.entity.PostRelayEntity

@Dao
interface PostInsertDao {

    @Transaction
    suspend fun insertPosts(relayedPosts: Collection<RelayedItem<PostEntity>>) {
        if (relayedPosts.isEmpty()) return

        internalInsertPost(posts = relayedPosts.map { it.item })
        val postRelays = relayedPosts.map {
            PostRelayEntity(postId = it.item.id, relayUrl = it.relayUrl)
        }
        internalInsertPostRelay(postRelays = postRelays)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertPost(posts: Collection<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun internalInsertPostRelay(postRelays: Collection<PostRelayEntity>)
}