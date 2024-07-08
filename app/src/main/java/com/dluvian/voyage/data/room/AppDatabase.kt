package com.dluvian.voyage.data.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.dluvian.voyage.data.room.dao.AccountDao
import com.dluvian.voyage.data.room.dao.BookmarkDao
import com.dluvian.voyage.data.room.dao.ContentSetDao
import com.dluvian.voyage.data.room.dao.CountDao
import com.dluvian.voyage.data.room.dao.DeleteDao
import com.dluvian.voyage.data.room.dao.EventRelayDao
import com.dluvian.voyage.data.room.dao.ExistsDao
import com.dluvian.voyage.data.room.dao.FriendDao
import com.dluvian.voyage.data.room.dao.FullProfileDao
import com.dluvian.voyage.data.room.dao.InboxDao
import com.dluvian.voyage.data.room.dao.ItemSetDao
import com.dluvian.voyage.data.room.dao.MuteDao
import com.dluvian.voyage.data.room.dao.Nip65Dao
import com.dluvian.voyage.data.room.dao.PostDao
import com.dluvian.voyage.data.room.dao.ProfileDao
import com.dluvian.voyage.data.room.dao.ReplyDao
import com.dluvian.voyage.data.room.dao.RootPostDao
import com.dluvian.voyage.data.room.dao.TopicDao
import com.dluvian.voyage.data.room.dao.VoteDao
import com.dluvian.voyage.data.room.dao.WebOfTrustDao
import com.dluvian.voyage.data.room.dao.tx.BookmarkUpsertDao
import com.dluvian.voyage.data.room.dao.tx.FriendUpsertDao
import com.dluvian.voyage.data.room.dao.tx.FullProfileUpsertDao
import com.dluvian.voyage.data.room.dao.tx.MuteUpsertDao
import com.dluvian.voyage.data.room.dao.tx.Nip65UpsertDao
import com.dluvian.voyage.data.room.dao.tx.PostInsertDao
import com.dluvian.voyage.data.room.dao.tx.ProfileSetUpsertDao
import com.dluvian.voyage.data.room.dao.tx.ProfileUpsertDao
import com.dluvian.voyage.data.room.dao.tx.TopicSetUpsertDao
import com.dluvian.voyage.data.room.dao.tx.TopicUpsertDao
import com.dluvian.voyage.data.room.dao.tx.WebOfTrustUpsertDao
import com.dluvian.voyage.data.room.entity.AccountEntity
import com.dluvian.voyage.data.room.entity.BookmarkEntity
import com.dluvian.voyage.data.room.entity.FriendEntity
import com.dluvian.voyage.data.room.entity.FullProfileEntity
import com.dluvian.voyage.data.room.entity.HashtagEntity
import com.dluvian.voyage.data.room.entity.MuteEntity
import com.dluvian.voyage.data.room.entity.Nip65Entity
import com.dluvian.voyage.data.room.entity.PostEntity
import com.dluvian.voyage.data.room.entity.ProfileEntity
import com.dluvian.voyage.data.room.entity.TopicEntity
import com.dluvian.voyage.data.room.entity.VoteEntity
import com.dluvian.voyage.data.room.entity.WebOfTrustEntity
import com.dluvian.voyage.data.room.entity.sets.ProfileSetEntity
import com.dluvian.voyage.data.room.entity.sets.ProfileSetItemEntity
import com.dluvian.voyage.data.room.entity.sets.TopicSetEntity
import com.dluvian.voyage.data.room.entity.sets.TopicSetItemEntity
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.data.room.view.EventRelayAuthorView
import com.dluvian.voyage.data.room.view.ReplyView
import com.dluvian.voyage.data.room.view.RootPostView
import com.dluvian.voyage.data.room.view.SimplePostView

@DeleteColumn(tableName = "vote", columnName = "isPositive")
class V10DeleteVoteIsPositiveColumn : AutoMigrationSpec

@Database(
    version = 14,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
        AutoMigration(from = 9, to = 10, spec = V10DeleteVoteIsPositiveColumn::class),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14),
    ],
    entities = [
        PostEntity::class,
        VoteEntity::class,
        AccountEntity::class,
        FriendEntity::class,
        WebOfTrustEntity::class,
        TopicEntity::class,
        HashtagEntity::class,
        Nip65Entity::class,
        ProfileEntity::class,
        FullProfileEntity::class,
        BookmarkEntity::class,
        ProfileSetEntity::class,
        ProfileSetItemEntity::class,
        TopicSetEntity::class,
        TopicSetItemEntity::class,
        MuteEntity::class,
    ],
    views = [
        RootPostView::class,
        AdvancedProfileView::class,
        EventRelayAuthorView::class,
        ReplyView::class,
        SimplePostView::class,
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun voteDao(): VoteDao
    abstract fun rootPostDao(): RootPostDao
    abstract fun topicDao(): TopicDao
    abstract fun friendDao(): FriendDao
    abstract fun webOfTrustDao(): WebOfTrustDao
    abstract fun nip65Dao(): Nip65Dao
    abstract fun profileDao(): ProfileDao
    abstract fun eventRelayDao(): EventRelayDao
    abstract fun replyDao(): ReplyDao
    abstract fun fullProfileDao(): FullProfileDao
    abstract fun postDao(): PostDao
    abstract fun inboxDao(): InboxDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun contentSetDao(): ContentSetDao
    abstract fun itemSetDao(): ItemSetDao
    abstract fun muteDao(): MuteDao

    // Util
    abstract fun deleteDao(): DeleteDao
    abstract fun countDao(): CountDao
    abstract fun existsDao(): ExistsDao

    // TX
    abstract fun postInsertDao(): PostInsertDao
    abstract fun friendUpsertDao(): FriendUpsertDao
    abstract fun webOfTrustUpsertDao(): WebOfTrustUpsertDao
    abstract fun topicUpsertDao(): TopicUpsertDao
    abstract fun bookmarkUpsertDao(): BookmarkUpsertDao
    abstract fun nip65UpsertDao(): Nip65UpsertDao
    abstract fun profileUpsertDao(): ProfileUpsertDao
    abstract fun fullProfileUpsertDao(): FullProfileUpsertDao
    abstract fun profileSetUpsertDao(): ProfileSetUpsertDao
    abstract fun topicSetUpsertDao(): TopicSetUpsertDao
    abstract fun muteUpsertDao(): MuteUpsertDao
}
