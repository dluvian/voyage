package com.dluvian.voyage.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dluvian.voyage.data.room.dao.AccountDao
import com.dluvian.voyage.data.room.dao.CommentDao
import com.dluvian.voyage.data.room.dao.EventRelayDao
import com.dluvian.voyage.data.room.dao.FriendDao
import com.dluvian.voyage.data.room.dao.Nip65Dao
import com.dluvian.voyage.data.room.dao.ProfileDao
import com.dluvian.voyage.data.room.dao.ResetDao
import com.dluvian.voyage.data.room.dao.RootPostDao
import com.dluvian.voyage.data.room.dao.TopicDao
import com.dluvian.voyage.data.room.dao.VoteDao
import com.dluvian.voyage.data.room.dao.WebOfTrustDao
import com.dluvian.voyage.data.room.dao.tx.FriendUpsertDao
import com.dluvian.voyage.data.room.dao.tx.Nip65UpsertDao
import com.dluvian.voyage.data.room.dao.tx.PostInsertDao
import com.dluvian.voyage.data.room.dao.tx.ProfileUpsertDao
import com.dluvian.voyage.data.room.dao.tx.TopicUpsertDao
import com.dluvian.voyage.data.room.dao.tx.VoteUpsertDao
import com.dluvian.voyage.data.room.dao.tx.WebOfTrustUpsertDao
import com.dluvian.voyage.data.room.entity.AccountEntity
import com.dluvian.voyage.data.room.entity.FriendEntity
import com.dluvian.voyage.data.room.entity.HashtagEntity
import com.dluvian.voyage.data.room.entity.Nip65Entity
import com.dluvian.voyage.data.room.entity.PostEntity
import com.dluvian.voyage.data.room.entity.PostRelayEntity
import com.dluvian.voyage.data.room.entity.ProfileEntity
import com.dluvian.voyage.data.room.entity.TopicEntity
import com.dluvian.voyage.data.room.entity.VoteEntity
import com.dluvian.voyage.data.room.entity.WebOfTrustEntity
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.data.room.view.CommentView
import com.dluvian.voyage.data.room.view.EventRelayAuthorView
import com.dluvian.voyage.data.room.view.RootPostView


// TODO: Reset before v0.1
@Database(
    version = 1,
    exportSchema = true,
    autoMigrations = [
//        AutoMigration(from = 1, to = 2),
    ],
    entities = [
        PostEntity::class,
        VoteEntity::class,
        PostRelayEntity::class,
        AccountEntity::class,
        FriendEntity::class,
        WebOfTrustEntity::class,
        TopicEntity::class,
        HashtagEntity::class,
        Nip65Entity::class,
        ProfileEntity::class,
    ],
    views = [
        RootPostView::class,
        AdvancedProfileView::class,
        EventRelayAuthorView::class,
        CommentView::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun postInsertDao(): PostInsertDao
    abstract fun voteDao(): VoteDao
    abstract fun rootPostDao(): RootPostDao
    abstract fun topicDao(): TopicDao
    abstract fun friendDao(): FriendDao
    abstract fun webOfTrustDao(): WebOfTrustDao
    abstract fun nip65Dao(): Nip65Dao
    abstract fun resetDao(): ResetDao
    abstract fun profileDao(): ProfileDao
    abstract fun eventRelayDao(): EventRelayDao
    abstract fun commentDao(): CommentDao

    // TX
    abstract fun voteUpsertDao(): VoteUpsertDao
    abstract fun friendUpsertDao(): FriendUpsertDao
    abstract fun webOfTrustUpsertDao(): WebOfTrustUpsertDao
    abstract fun topicUpsertDao(): TopicUpsertDao
    abstract fun nip65UpsertDao(): Nip65UpsertDao
    abstract fun profileUpsertDao(): ProfileUpsertDao
}
