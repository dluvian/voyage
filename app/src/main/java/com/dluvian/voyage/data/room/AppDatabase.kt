package com.dluvian.voyage.data.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import com.dluvian.voyage.data.room.dao.AccountDao
import com.dluvian.voyage.data.room.dao.BookmarkDao
import com.dluvian.voyage.data.room.dao.ContentSetDao
import com.dluvian.voyage.data.room.dao.EventRelayDao
import com.dluvian.voyage.data.room.dao.FriendDao
import com.dluvian.voyage.data.room.dao.FullProfileDao
import com.dluvian.voyage.data.room.dao.HashtagDao
import com.dluvian.voyage.data.room.dao.HomeFeedDao
import com.dluvian.voyage.data.room.dao.InboxDao
import com.dluvian.voyage.data.room.dao.ItemSetDao
import com.dluvian.voyage.data.room.dao.LockDao
import com.dluvian.voyage.data.room.dao.MuteDao
import com.dluvian.voyage.data.room.dao.Nip65Dao
import com.dluvian.voyage.data.room.dao.PostDao
import com.dluvian.voyage.data.room.dao.ProfileDao
import com.dluvian.voyage.data.room.dao.ReplyDao
import com.dluvian.voyage.data.room.dao.RootPostDao
import com.dluvian.voyage.data.room.dao.TopicDao
import com.dluvian.voyage.data.room.dao.VoteDao
import com.dluvian.voyage.data.room.dao.WebOfTrustDao
import com.dluvian.voyage.data.room.dao.insert.LockInsertDao
import com.dluvian.voyage.data.room.dao.insert.MainEventInsertDao
import com.dluvian.voyage.data.room.dao.upsert.BookmarkUpsertDao
import com.dluvian.voyage.data.room.dao.upsert.FriendUpsertDao
import com.dluvian.voyage.data.room.dao.upsert.FullProfileUpsertDao
import com.dluvian.voyage.data.room.dao.upsert.MuteUpsertDao
import com.dluvian.voyage.data.room.dao.upsert.Nip65UpsertDao
import com.dluvian.voyage.data.room.dao.upsert.ProfileSetUpsertDao
import com.dluvian.voyage.data.room.dao.upsert.ProfileUpsertDao
import com.dluvian.voyage.data.room.dao.upsert.TopicSetUpsertDao
import com.dluvian.voyage.data.room.dao.upsert.TopicUpsertDao
import com.dluvian.voyage.data.room.dao.upsert.WebOfTrustUpsertDao
import com.dluvian.voyage.data.room.dao.util.CountDao
import com.dluvian.voyage.data.room.dao.util.DeleteDao
import com.dluvian.voyage.data.room.dao.util.ExistsDao
import com.dluvian.voyage.data.room.entity.AccountEntity
import com.dluvian.voyage.data.room.entity.FullProfileEntity
import com.dluvian.voyage.data.room.entity.LockEntity
import com.dluvian.voyage.data.room.entity.ProfileEntity
import com.dluvian.voyage.data.room.entity.lists.BookmarkEntity
import com.dluvian.voyage.data.room.entity.lists.FriendEntity
import com.dluvian.voyage.data.room.entity.lists.MuteEntity
import com.dluvian.voyage.data.room.entity.lists.Nip65Entity
import com.dluvian.voyage.data.room.entity.lists.TopicEntity
import com.dluvian.voyage.data.room.entity.lists.WebOfTrustEntity
import com.dluvian.voyage.data.room.entity.main.CrossPostMetaEntity
import com.dluvian.voyage.data.room.entity.main.HashtagEntity
import com.dluvian.voyage.data.room.entity.main.LegacyReplyMetaEntity
import com.dluvian.voyage.data.room.entity.main.MainEventEntity
import com.dluvian.voyage.data.room.entity.main.RootPostMetaEntity
import com.dluvian.voyage.data.room.entity.main.VoteEntity
import com.dluvian.voyage.data.room.entity.sets.ProfileSetEntity
import com.dluvian.voyage.data.room.entity.sets.ProfileSetItemEntity
import com.dluvian.voyage.data.room.entity.sets.TopicSetEntity
import com.dluvian.voyage.data.room.entity.sets.TopicSetItemEntity
import com.dluvian.voyage.data.room.view.AdvancedProfileView
import com.dluvian.voyage.data.room.view.CrossPostView
import com.dluvian.voyage.data.room.view.EventRelayAuthorView
import com.dluvian.voyage.data.room.view.LegacyReplyView
import com.dluvian.voyage.data.room.view.RootPostView
import com.dluvian.voyage.data.room.view.SimplePostView

@DeleteColumn(tableName = "vote", columnName = "isPositive")
class V10DeleteVoteIsPositiveColumn : AutoMigrationSpec

@Database(
    version = 24,
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
        AutoMigration(from = 14, to = 15),
        AutoMigration(from = 15, to = 16),
        AutoMigration(from = 16, to = 17),
        AutoMigration(from = 17, to = 18),
        AutoMigration(from = 18, to = 19),
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21),
        AutoMigration(from = 21, to = 22),
        AutoMigration(from = 22, to = 23),
        AutoMigration(from = 23, to = 24),
    ],
    entities = [
        // Main
        MainEventEntity::class,
        RootPostMetaEntity::class,
        LegacyReplyMetaEntity::class,
        CrossPostMetaEntity::class,
        HashtagEntity::class,
        VoteEntity::class,

        // Lists
        FriendEntity::class,
        WebOfTrustEntity::class,
        TopicEntity::class,
        Nip65Entity::class,
        BookmarkEntity::class,
        MuteEntity::class,

        // Sets
        ProfileSetEntity::class,
        ProfileSetItemEntity::class,
        TopicSetEntity::class,
        TopicSetItemEntity::class,

        // Other
        AccountEntity::class,
        ProfileEntity::class,
        FullProfileEntity::class,
        LockEntity::class,
    ],
    views = [
        SimplePostView::class,
        EventRelayAuthorView::class,
        RootPostView::class,
        LegacyReplyView::class,
        CrossPostView::class,
        AdvancedProfileView::class,
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun voteDao(): VoteDao
    abstract fun rootPostDao(): RootPostDao
    abstract fun homeFeedDao(): HomeFeedDao
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
    abstract fun lockDao(): LockDao
    abstract fun hashtagDao(): HashtagDao

    // Util
    abstract fun deleteDao(): DeleteDao
    abstract fun countDao(): CountDao
    abstract fun existsDao(): ExistsDao

    // Insert
    abstract fun mainEventInsertDao(): MainEventInsertDao
    abstract fun lockInsertDao(): LockInsertDao

    // Upsert
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
