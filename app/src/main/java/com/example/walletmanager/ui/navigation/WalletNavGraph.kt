package com.example.walletmanager.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.walletmanager.data.repository.WalletRepository
import com.example.walletmanager.notifications.ReminderDraft
import com.example.walletmanager.notifications.ReminderScheduler
import com.example.walletmanager.notifications.IpoScheduler
import com.example.walletmanager.notifications.FixedDepositScheduler
import com.example.walletmanager.data.model.PendingIpo
import com.example.walletmanager.ui.WalletViewModel
import com.example.walletmanager.ui.screen.accounts.AccountsScreen
import com.example.walletmanager.ui.screen.accounts.AccountDetailScreen
import com.example.walletmanager.ui.screen.home.HomeScreen
import com.example.walletmanager.ui.screen.filters.FilterScreen
import com.example.walletmanager.ui.screen.investments.InvestmentsScreen
import com.example.walletmanager.ui.screen.more.MoreScreen
import com.example.walletmanager.ui.screen.plans.PlansScreen
import com.example.walletmanager.ui.screen.reports.MonthlyScreen
import com.example.walletmanager.ui.screen.reports.CalendarScreen
import com.example.walletmanager.ui.screen.reports.WeeklyScreen
import com.example.walletmanager.ui.screen.stats.StatsScreen
import com.example.walletmanager.ui.component.LocalProfileHeader
import com.example.walletmanager.ui.component.ProfileHeaderState
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun WalletNavGraph(vm: WalletViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val repository: WalletRepository = koinInject()
    val navController = rememberNavController()
    val context = LocalContext.current
    var pendingReminder by remember { mutableStateOf<ReminderDraft?>(null) }
    var pendingIpo by remember { mutableStateOf<PendingIpo?>(null) }

    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pendingReminder?.let {
            if (granted) {
                ReminderScheduler.schedule(context, it)
            } else {
                vm.notify("Reminder permission denied")
            }
        }
        pendingReminder = null
        pendingIpo?.let {
            if (granted) {
                IpoScheduler.schedule(context, it)
            } else {
                vm.notify("IPO notification permission denied")
            }
        }
        pendingIpo = null
    }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                it.write(repository.exportCsv(state))
            }
            vm.notify("CSV exported")
        }
    }

    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                vm.importCsv(it.readText())
            }
        }
    }

    fun open(route: WalletRoute) {
        vm.navigate(route::class.simpleName.orEmpty())
        navController.navigate(route) { launchSingleTop = true }
    }

    fun addReminder(reminder: ReminderDraft) {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingReminder = reminder
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            ReminderScheduler.schedule(context, reminder)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        vm.events.collect { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(state.fixedDeposits, state.accounts) {
        state.fixedDeposits.forEach { deposit ->
            if (deposit.maturityDateMillis > System.currentTimeMillis()) {
                val bankName = state.accounts.firstOrNull { it.id == deposit.accountId }?.name ?: ""
                FixedDepositScheduler.schedule(context, deposit, bankName)
            }
        }
    }

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    CompositionLocalProvider(
        LocalProfileHeader provides ProfileHeaderState(
            name = state.profileName,
            imageUri = state.profileImageUri
        )
    ) {
        val navigationBackdrop = rememberLayerBackdrop()
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(navigationBackdrop)
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute,
                        modifier = Modifier.padding(padding)
                    ) {
            composable<HomeRoute> {
                HomeScreen(
                    state = state,
                    toggleBalances = vm::toggleBalances,
                    addTransaction = vm::addTransaction,
                    transfer = vm::transfer,
                    addCategory = vm::addCategory,
                    editCategory = vm::editCategory,
                    deleteCategory = vm::deleteCategory,
                    editTransaction = vm::editTransaction,
                    deleteTransaction = vm::deleteTransaction,
                    open = ::open
                )
            }
            composable<FiltersRoute> {
                FilterScreen(
                    state = state,
                    apply = { accountId, category, kind ->
                        vm.setTransactionFilters(accountId, category, kind)
                        vm.navigate(HomeRoute::class.simpleName.orEmpty())
                        navController.popBackStack()
                    },
                    clear = {
                        vm.clearTransactionFilters()
                        vm.navigate(HomeRoute::class.simpleName.orEmpty())
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable<AccountsRoute> {
                AccountsScreen(
                    state = state,
                    open = ::open,
                    addAccount = vm::addAccount,
                    addFixedDeposit = {
                        vm.addFixedDeposit(it)
                        val bankName = state.accounts.firstOrNull { acc -> acc.id == it.accountId }?.name ?: ""
                        FixedDepositScheduler.schedule(context, it, bankName)
                    },
                    editFixedDeposit = {
                        vm.editFixedDeposit(it)
                        val bankName = state.accounts.firstOrNull { acc -> acc.id == it.accountId }?.name ?: ""
                        FixedDepositScheduler.schedule(context, it, bankName)
                    },
                    deleteFixedDeposit = vm::deleteFixedDeposit
                )
            }
            composable<AccountDetailsRoute> { entry ->
                AccountDetailScreen(
                    state = state,
                    accountId = entry.toRoute<AccountDetailsRoute>().accountId,
                    goBack = { navController.popBackStack() },
                    editAccount = vm::editAccount,
                    deleteAccount = vm::deleteAccount,
                    editTransaction = vm::editTransaction,
                    deleteTransaction = vm::deleteTransaction,
                    addCategory = vm::addCategory,
                    editCategory = vm::editCategory,
                    deleteCategory = vm::deleteCategory
                )
            }
            composable<StatsRoute> {
                StatsScreen(
                    state = state,
                    editTransaction = vm::editTransaction,
                    deleteTransaction = vm::deleteTransaction,
                    addCategory = vm::addCategory,
                    editCategory = vm::editCategory,
                    deleteCategory = vm::deleteCategory
                )
            }
            composable<InvestmentsRoute> {
                InvestmentsScreen(
                    state = state,
                    addHolding = vm::addHolding,
                    editHolding = vm::editHolding,
                    addIpo = { ipo: PendingIpo ->
                        vm.addIpo(ipo)
                        if (
                            Build.VERSION.SDK_INT >= 33 &&
                            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        ) {
                            pendingIpo = ipo
                            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            IpoScheduler.schedule(context, ipo)
                        }
                    },
                    rejectIpo = vm::rejectIpo
                )
            }
            composable<WeeklyRoute> {
                WeeklyScreen(state) { navController.popBackStack() }
            }
            composable<MonthlyRoute> {
                MonthlyScreen(state) { navController.popBackStack() }
            }
            composable<CalendarRoute> {
                CalendarScreen(state) { navController.popBackStack() }
            }
            composable<PlansRoute> {
                PlansScreen(
                    state = state,
                    addPlan = { name, amount, frequency, cashback, investment, accountId, investmentType ->
                        vm.addRecurringPayment(
                            name = name,
                            amountMinor = amount,
                            frequency = frequency,
                            cashbackMinor = cashback,
                            accountId = accountId,
                            investmentType = investmentType,
                            category = if (investment) "Investments" else "Bills"
                        )
                    },
                    editPlan = vm::editRecurringPayment,
                    deletePlan = vm::deleteRecurringPayment,
                    onBack = { navController.popBackStack() },
                    togglePlan = { id ->
                        state.recurringPayments
                            .firstOrNull { it.id == id }
                            ?.let(vm::toggleRecurringPayment)
                    }
                )
            }
            composable<MoreRoute> { entry ->
                val route = entry.toRoute<MoreRoute>()
                MoreScreen(
                    state = state,
                    setCurrency = vm::setCurrency,
                    addNote = vm::addNote,
                    export = { export.launch("wallet-manager-export.csv") },
                    import = {
                        import.launch(
                            arrayOf(
                                "text/csv",
                                "text/comma-separated-values",
                                "application/csv"
                            )
                        )
                    },
                    addReminder = ::addReminder,
                    setProfile = vm::setProfile,
                    wipeData = vm::wipeData,
                    onBack = if (route.openProfile) ({ navController.popBackStack() }) else null,
                    openProfileOnEntry = route.openProfile
                )
            }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    )
            ) {
                WalletNavigationBar(
                    currentRoute = currentDestination?.route,
                    backdrop = navigationBackdrop,
                    onNavigate = ::open
                )
            }
        }
    }
}
