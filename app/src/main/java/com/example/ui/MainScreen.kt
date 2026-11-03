package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FamilyMember
import com.example.ui.components.AddAccountDialog
import com.example.ui.components.AddMemberDialog
import com.example.ui.components.AddTransactionBottomSheet
import com.example.ui.components.AdminSwitchDialog
import com.example.ui.components.ShareUtils
import com.example.ui.screens.*

sealed class NavTab(val title: String, val icon: ImageVector, val tag: String) {
    object Dashboard : NavTab("Beranda", Icons.Default.Dashboard, "nav_tab_dashboard")
    object Transactions : NavTab("Transaksi", Icons.Default.ReceiptLong, "nav_tab_transactions")
    object Members : NavTab("Anggota", Icons.Default.Group, "nav_tab_members")
    object Accounts : NavTab("Rekening", Icons.Default.AccountBalance, "nav_tab_accounts")
    object Reports : NavTab("Laporan", Icons.Default.Assessment, "nav_tab_reports")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf<NavTab>(NavTab.Dashboard) }

    val activeAdmin by viewModel.activeAdmin.collectAsStateWithLifecycle()
    val admins by viewModel.admins.collectAsStateWithLifecycle()
    val members by viewModel.members.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val pendingTransactions by viewModel.pendingTransactions.collectAsStateWithLifecycle()
    val ledgerEntries by viewModel.ledgerEntries.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog & Bottom Sheet States
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var showSwitchAdminDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }

    // Handle back button when not on Dashboard
    BackHandler(enabled = currentTab != NavTab.Dashboard) {
        currentTab = NavTab.Dashboard
    }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    val tabs = listOf(
        NavTab.Dashboard,
        NavTab.Transactions,
        NavTab.Members,
        NavTab.Accounts,
        NavTab.Reports
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "FAMILY SAVINGS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSwitchAdminDialog = true },
                        modifier = Modifier.testTag("topbar_admin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Ganti Admin",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.Dashboard -> DashboardScreen(
                    activeAdmin = activeAdmin,
                    members = members,
                    accounts = accounts,
                    transactions = transactions,
                    pendingTransactions = pendingTransactions,
                    onOpenAddTransaction = { showAddTransactionSheet = true },
                    onOpenSwitchAdmin = { showSwitchAdminDialog = true },
                    onSyncOfflineQueue = { viewModel.syncOfflineQueue() },
                    onNavigateToTransactions = { currentTab = NavTab.Transactions },
                    onNavigateToAccounts = { currentTab = NavTab.Accounts },
                    onNavigateToMembers = { currentTab = NavTab.Members }
                )
                NavTab.Transactions -> {
                    val context = LocalContext.current
                    TransactionsScreen(
                        transactions = transactions,
                        onVoidTransaction = { id, reason -> viewModel.voidTransaction(id, reason) },
                        onOpenAddTransaction = { showAddTransactionSheet = true },
                        onShareReceipt = { txn ->
                            val text = viewModel.generateReceiptText(txn)
                            ShareUtils.shareToWhatsAppOrNotes(context, text, "Bukti Transaksi — ${txn.id}")
                        }
                    )
                }
                NavTab.Members -> MembersScreen(
                    members = members,
                    onOpenAddMember = { showAddMemberDialog = true },
                    onOpenDepositForMember = { _ -> showAddTransactionSheet = true }
                )
                NavTab.Accounts -> AccountsScreen(
                    accounts = accounts,
                    onOpenAddAccount = { showAddAccountDialog = true },
                    onOpenTransfer = { showAddTransactionSheet = true }
                )
                NavTab.Reports -> ReportsAuditScreen(
                    ledgerEntries = ledgerEntries,
                    auditLogs = auditLogs,
                    onExportJson = { callback -> viewModel.exportBackup(callback) },
                    onImportJson = { json -> viewModel.importBackup(json) },
                    onExportCsv = { callback -> viewModel.exportCsv(callback) },
                    onGenerateWhatsAppReport = { callback -> viewModel.generateWhatsAppFullReport(callback) }
                )
            }
        }
    }

    // Modal Bottom Sheets & Dialogs
    if (showAddTransactionSheet) {
        AddTransactionBottomSheet(
            members = members,
            accounts = accounts,
            onDismiss = { showAddTransactionSheet = false },
            onSubmit = { type, amount, memberId, sourceAcc, destAcc, category, notes, isOffline ->
                viewModel.recordTransaction(
                    type = type,
                    amount = amount,
                    memberId = memberId,
                    sourceAccountId = sourceAcc,
                    destAccountId = destAcc,
                    category = category,
                    notes = notes,
                    isOfflinePending = isOffline
                )
                showAddTransactionSheet = false
            }
        )
    }

    if (showSwitchAdminDialog) {
        AdminSwitchDialog(
            admins = admins,
            currentActiveAdmin = activeAdmin,
            onDismiss = { showSwitchAdminDialog = false },
            onConfirmSwitch = { adminId, pin ->
                viewModel.switchAdmin(adminId, pin)
                showSwitchAdminDialog = false
            }
        )
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onConfirm = { name, relationship, target, notes ->
                viewModel.addMember(name, relationship, target, notes)
                showAddMemberDialog = false
            }
        )
    }

    if (showAddAccountDialog) {
        AddAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onConfirm = { name, type, number, balance, isFamilyPool, notes ->
                viewModel.addAccount(name, type, number, balance, isFamilyPool, notes)
                showAddAccountDialog = false
            }
        )
    }
}
