package com.dluvian.voyage.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dluvian.voyage.data.room.dao.AccountDao
import com.dluvian.voyage.data.room.dao.FriendUpsertDao
import com.dluvian.voyage.data.room.dao.PostInsertDao
import com.dluvian.voyage.data.room.dao.TopicUpsertDao
import com.dluvian.voyage.data.room.dao.VoteUpsertDao
import com.dluvian.voyage.data.room.dao.WebOfTrustUpsertDao
import com.dluvian.voyage.data.room.entity.AccountEntity
import com.dluvian.voyage.data.room.entity.FriendEntity
import com.dluvian.voyage.data.room.entity.PostEntity
import com.dluvian.voyage.data.room.entity.PostRelayEntity
import com.dluvian.voyage.data.room.entity.TopicEntity
import com.dluvian.voyage.data.room.entity.VoteEntity
import com.dluvian.voyage.data.room.entity.WebOfTrustEntity

// TODO: Reset to v=1 when first releasing app, and delete migration files
@Database(
    version = 1,
    exportSchema = true,
    autoMigrations = [
//        AutoMigration(from = 1, to = 2)
    ],
    entities = [
        PostEntity::class,
        VoteEntity::class,
        PostRelayEntity::class,
        AccountEntity::class,
        FriendEntity::class,
        WebOfTrustEntity::class,
        TopicEntity::class
    ],
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun postInsertDao(): PostInsertDao
    abstract fun voteUpsertDao(): VoteUpsertDao
    abstract fun friendUpsertDao(): FriendUpsertDao
    abstract fun webOfTrustUpsertDao(): WebOfTrustUpsertDao
    abstract fun topicUpsertDao(): TopicUpsertDao

}
