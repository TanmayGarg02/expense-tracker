package com.example.walletmanager.data.local

import android.content.Context
import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        RecurringPaymentEntity::class,
        HoldingEntity::class,
        IpoEntity::class,
        FixedDepositEntity::class,
        NoteEntity::class,
        SettingsEntity::class,
        CategoryEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class WalletDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao

    companion object {
        @Volatile
        private var instance: WalletDatabase? = null

        fun getInstance(context: Context): WalletDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                WalletDatabase::class.java,
                "wallet_manager.db"
            ).addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10
            ).fallbackToDestructiveMigration().build().also {
                instance = it
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN note TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS categories (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, parentId TEXT)"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN cashbackMinor INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recurring_payments ADD COLUMN cashbackMinor INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE settings ADD COLUMN profileName TEXT NOT NULL DEFAULT 'Alex'"
                )
                database.execSQL(
                    "ALTER TABLE settings ADD COLUMN profileImageUri TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS ipos (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, amountMinor INTEGER NOT NULL, allotmentDate TEXT NOT NULL, allotmentDateMillis INTEGER NOT NULL)"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recurring_payments ADD COLUMN investmentType TEXT NOT NULL DEFAULT 'MUTUAL_FUND'"
                )
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS fixed_deposits (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, principalMinor INTEGER NOT NULL, interestRateBps INTEGER NOT NULL, maturityDate TEXT NOT NULL, maturityDateMillis INTEGER NOT NULL, maturityAmountMinor INTEGER NOT NULL)"
                )
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE fixed_deposits ADD COLUMN accountId TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}
