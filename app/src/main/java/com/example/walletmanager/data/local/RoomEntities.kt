package com.example.walletmanager.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(@PrimaryKey val id: String, val name: String, val type: String, val balanceMinor: Long, val accent: String)

@Entity(tableName = "transactions")
data class TransactionEntity(@PrimaryKey val id: String, val title: String, val category: String, val amountMinor: Long, val kind: String, val accountId: String, val date: String, val weekOfMonth: Int, val note: String = "", val cashbackMinor: Long = 0, val createdAt: Long = 0)

@Entity(tableName = "categories")
data class CategoryEntity(@PrimaryKey val id: String, val name: String, val parentId: String? = null)

@Entity(tableName = "recurring_payments")
data class RecurringPaymentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amountMinor: Long,
    val frequency: String,
    val due: String,
    val accountId: String,
    val category: String,
    val active: Boolean,
    val dueDay: Int,
    val lastProcessedPeriod: String,
    val cashbackMinor: Long = 0,
    val investmentType: String = "MUTUAL_FUND"
)

@Entity(tableName = "holdings")
data class HoldingEntity(@PrimaryKey val id: String, val name: String, val type: String, val units: String, val valueMinor: Long, val costMinor: Long, val recurringPaymentId: String?)

@Entity(tableName = "ipos")
data class IpoEntity(
    @PrimaryKey val id: String,
    val name: String,
    val amountMinor: Long,
    val allotmentDate: String,
    val allotmentDateMillis: Long
)

@Entity(tableName = "notes")
data class NoteEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val text: String)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val baseCurrency: String = "INR",
    val profileName: String = "Tanmay",
    val profileImageUri: String = ""
)
