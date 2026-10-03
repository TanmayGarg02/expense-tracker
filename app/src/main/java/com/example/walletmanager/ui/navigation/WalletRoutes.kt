package com.example.walletmanager.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface WalletRoute

@Serializable
data object HomeRoute : WalletRoute

@Serializable
data object AccountsRoute : WalletRoute

@Serializable
data class AccountDetailsRoute(val accountId: String) : WalletRoute

@Serializable
data object StatsRoute : WalletRoute

@Serializable
data object InvestmentsRoute : WalletRoute

@Serializable
data object WeeklyRoute : WalletRoute

@Serializable
data object MonthlyRoute : WalletRoute

@Serializable
data object CalendarRoute : WalletRoute

@Serializable
data object PlansRoute : WalletRoute

@Serializable
data class MoreRoute(val openProfile: Boolean = false) : WalletRoute

@Serializable
data object FiltersRoute : WalletRoute
