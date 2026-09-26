package com.example.walletmanager.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM accounts ORDER BY id") fun accounts(): Flow<List<AccountEntity>>
    @Query("SELECT * FROM transactions ORDER BY rowid DESC") fun transactions(): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM recurring_payments ORDER BY id") fun recurringPayments(): Flow<List<RecurringPaymentEntity>>
    @Query("SELECT * FROM holdings ORDER BY id") fun holdings(): Flow<List<HoldingEntity>>
    @Query("SELECT * FROM ipos ORDER BY allotmentDateMillis") fun ipos(): Flow<List<IpoEntity>>
    @Query("SELECT * FROM notes ORDER BY id DESC") fun notes(): Flow<List<NoteEntity>>
    @Query("SELECT * FROM categories ORDER BY parentId, name") fun categories(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM categories WHERE id = :id") suspend fun category(id: String): CategoryEntity?
    @Query("SELECT * FROM settings WHERE id = 1") fun settings(): Flow<SettingsEntity?>
    @Query("SELECT COUNT(*) FROM accounts") suspend fun accountCount(): Int
    @Query("SELECT COUNT(*) FROM categories") suspend fun categoryCount(): Int
    @Query("SELECT * FROM accounts WHERE id = :id") suspend fun account(id: String): AccountEntity?
    @Query("SELECT * FROM recurring_payments") suspend fun currentRecurringPayments(): List<RecurringPaymentEntity>
    @Query("SELECT * FROM transactions WHERE id = :id") suspend fun transaction(id: String): TransactionEntity?
    @Query("SELECT * FROM holdings WHERE recurringPaymentId = :paymentId LIMIT 1") suspend fun holdingForRecurringPayment(paymentId: String): HoldingEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAccounts(items: List<AccountEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTransactions(items: List<TransactionEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertRecurringPayments(items: List<RecurringPaymentEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertHoldings(items: List<HoldingEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertNotes(items: List<NoteEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertCategories(items: List<CategoryEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSettings(item: SettingsEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAccount(item: AccountEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTransaction(item: TransactionEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertRecurringPayment(item: RecurringPaymentEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertHolding(item: HoldingEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertIpo(item: IpoEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertCategory(item: CategoryEntity)
    @Query("DELETE FROM accounts WHERE id = :id") suspend fun deleteAccount(id: String)
    @Query("DELETE FROM transactions WHERE id = :id") suspend fun deleteTransaction(id: String)
    @Query("DELETE FROM transactions WHERE accountId = :accountId") suspend fun deleteAccountTransactions(accountId: String)
    @Query("DELETE FROM categories WHERE id = :id") suspend fun deleteCategory(id: String)
    @Query("DELETE FROM categories WHERE parentId = :parentId") suspend fun deleteSubcategories(parentId: String)
    @Query("UPDATE accounts SET balanceMinor = balanceMinor + :delta WHERE id = :accountId") suspend fun adjustBalance(accountId: String, delta: Long)
    @Query("DELETE FROM accounts") suspend fun clearAccounts()
    @Query("DELETE FROM transactions") suspend fun clearTransactions()
    @Query("DELETE FROM recurring_payments") suspend fun clearRecurringPayments()
    @Query("DELETE FROM holdings") suspend fun clearHoldings()
    @Query("DELETE FROM notes") suspend fun clearNotes()
    @Query("DELETE FROM categories") suspend fun clearCategories()
    @Query("DELETE FROM ipos") suspend fun clearIpos()
    @Query("DELETE FROM settings") suspend fun clearSettings()
    @Query("DELETE FROM ipos WHERE id = :id") suspend fun deleteIpo(id: String)
    @Query("SELECT * FROM ipos WHERE id = :id") suspend fun ipo(id: String): IpoEntity?
}
