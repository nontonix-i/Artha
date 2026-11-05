package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.AuthManager
import com.example.data.model.UserProfile
import com.example.data.repository.ArthaStockRepository
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authManager = AuthManager(this)

        setContent {
            ArthaStockTheme {
                val scope = rememberCoroutineScope()
                val currentUser by authManager.currentUser.collectAsState()

                LaunchedEffect(Unit) {
                    authManager.attemptAutoSignIn(scope)
                }

                if (currentUser == null) {
                    SignInScreen(authManager = authManager)
                } else {
                    val user = currentUser!!
                    val repository = remember { ArthaStockRepository(this@MainActivity) }

                    // Ensure user profile in Firestore
                    LaunchedEffect(user.uid) {
                        repository.saveUserProfile(
                            UserProfile(
                                uid = user.uid,
                                displayName = user.displayName ?: user.email ?: "Pengguna Sales",
                                email = user.email ?: "",
                                role = "SALES"
                            )
                        )
                    }

                    val mainViewModel: MainViewModel = viewModel(
                        factory = MainViewModelFactory(
                            repository = repository,
                            currentUserId = user.uid,
                            currentUserName = user.displayName ?: user.email ?: "Sales"
                        )
                    )

                    MainAppScaffold(
                        viewModel = mainViewModel,
                        userName = user.displayName ?: user.email ?: "Sales",
                        userEmail = user.email ?: "",
                        onSignOut = {
                            authManager.signOut(scope)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: MainViewModel,
    userName: String,
    userEmail: String,
    onSignOut: () -> Unit
) {
    var selectedScreenIndex by remember { mutableStateOf(0) }
    var showSellDialog by remember { mutableStateOf(false) }
    var showPoolDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showDebtDialog by remember { mutableStateOf(false) }
    var showNewProductDialog by remember { mutableStateOf(false) }
    var showDepositPaymentDialog by remember { mutableStateOf(false) }
    var showSavingsDepositDialog by remember { mutableStateOf(false) }
    var showSavingsWithdrawDialog by remember { mutableStateOf(false) }
    var showDebtPaymentDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    val products by viewModel.products.collectAsState()
    val teamUsers by viewModel.teamUsers.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val metrics by viewModel.financialMetrics.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearStatusMessage()
            }
        }
    }

    // Handle back button on sub-screens
    BackHandler(enabled = selectedScreenIndex != 0) {
        selectedScreenIndex = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CobaltPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_arthastock_logo),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ArthaStock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = userName,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSignOutConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Keluar Akun",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureWhite,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                contentColor = CobaltPrimary,
                tonalElevation = 2.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = selectedScreenIndex == 0,
                    onClick = { selectedScreenIndex = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CobaltPrimary,
                        selectedTextColor = CobaltPrimary,
                        indicatorColor = CobaltContainer,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedScreenIndex == 1,
                    onClick = { selectedScreenIndex = 1 },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Inventaris") },
                    label = { Text("Jatah Stok", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CobaltPrimary,
                        selectedTextColor = CobaltPrimary,
                        indicatorColor = CobaltContainer,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedScreenIndex == 2,
                    onClick = { selectedScreenIndex = 2 },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Operasional") },
                    label = { Text("Operasional", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CobaltPrimary,
                        selectedTextColor = CobaltPrimary,
                        indicatorColor = CobaltContainer,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedScreenIndex == 3,
                    onClick = { selectedScreenIndex = 3 },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Riwayat") },
                    label = { Text("Riwayat", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CobaltPrimary,
                        selectedTextColor = CobaltPrimary,
                        indicatorColor = CobaltContainer,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedScreenIndex) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onOpenSellDialog = { showSellDialog = true },
                    onOpenTransferDialog = { showTransferDialog = true },
                    onOpenPoolDialog = { showPoolDialog = true },
                    onOpenDebtDialog = { showDebtDialog = true },
                    onOpenDepositPaymentDialog = { showDepositPaymentDialog = true },
                    onOpenSavingsDepositDialog = { showSavingsDepositDialog = true },
                    onOpenSavingsWithdrawDialog = { showSavingsWithdrawDialog = true },
                    onOpenDebtPaymentDialog = { showDebtPaymentDialog = true },
                    onViewAllHistory = { selectedScreenIndex = 3 }
                )
                1 -> InventoryScreen(
                    viewModel = viewModel,
                    onOpenSellDialog = { showSellDialog = true },
                    onOpenPoolDialog = { showPoolDialog = true },
                    onOpenNewProductDialog = { showNewProductDialog = true }
                )
                2 -> OperationsScreen(
                    viewModel = viewModel,
                    onOpenTransferDialog = { showTransferDialog = true },
                    onOpenDebtDialog = { showDebtDialog = true },
                    onOpenDebtPaymentDialog = { showDebtPaymentDialog = true }
                )
                3 -> HistoryScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    // Sell Dialog with Mandatory Deposit & Savings Checklist Toggle
    if (showSellDialog) {
        SellDialog(
            products = products,
            onDismiss = { showSellDialog = false },
            onConfirmSale = { prod, qty, isSavingsIncluded, note ->
                viewModel.recordSale(prod, qty, isSavingsIncluded, note) {
                    showSellDialog = false
                }
            }
        )
    }

    // Weekly Pool Distribution Dialog
    if (showPoolDialog) {
        PoolDistributionDialog(
            products = products,
            teamUsers = teamUsers,
            currentUserId = viewModel.currentUserId,
            onDismiss = { showPoolDialog = false },
            onConfirmDistribution = { prod, weekId, poolQty, allocs ->
                viewModel.distributePool(prod, weekId, poolQty, allocs) {
                    showPoolDialog = false
                }
            }
        )
    }

    // Transfer Goods Dialog
    if (showTransferDialog) {
        TransferDialog(
            products = products,
            teamUsers = teamUsers,
            currentUserId = viewModel.currentUserId,
            onDismiss = { showTransferDialog = false },
            onConfirmTransfer = { toUser, prod, qty, note ->
                viewModel.transferGoods(toUser, prod, qty, note) {
                    showTransferDialog = false
                }
            }
        )
    }

    // Debt & Loan Dialog (Catat baru dengan HPP untuk pinjam barang & optional note)
    if (showDebtDialog) {
        DebtDialog(
            products = products,
            onDismiss = { showDebtDialog = false },
            onConfirmDebt = { type, prod, qty, nominal, note ->
                viewModel.recordDebt(type, prod, qty, nominal, note) {
                    showDebtDialog = false
                }
            }
        )
    }

    // Bayar Setoran Pabrik Dialog (Bisa filter & setor pisah per merk/pabrik)
    if (showDepositPaymentDialog) {
        val pendingTxs = transactions.filter { !it.isDepositSettled }
        DepositPaymentDialog(
            pendingTransactions = pendingTxs,
            onDismiss = { showDepositPaymentDialog = false },
            onSettleSingle = { txId ->
                viewModel.settleDeposit(txId)
            },
            onSettleAll = { txIds ->
                viewModel.settleDepositsList(txIds) {
                    showDepositPaymentDialog = false
                }
            }
        )
    }

    // Tambah Tabungan Langsung (Nabung Mandiri) Dialog
    if (showSavingsDepositDialog) {
        SavingsDepositDialog(
            currentSavingsBalance = metrics.totalSavings,
            onDismiss = { showSavingsDepositDialog = false },
            onConfirmDeposit = { nominal, note ->
                viewModel.addManualSavings(nominal, note) {
                    showSavingsDepositDialog = false
                }
            }
        )
    }

    // Ambil Tabungan (Penarikan) Dialog
    if (showSavingsWithdrawDialog) {
        SavingsWithdrawDialog(
            currentSavingsBalance = metrics.totalSavings,
            onDismiss = { showSavingsWithdrawDialog = false },
            onConfirmWithdraw = { nominal, note ->
                viewModel.withdrawSavings(nominal, note) {
                    showSavingsWithdrawDialog = false
                }
            }
        )
    }

    // Bayar / Pelunasan Hutang Dialog
    if (showDebtPaymentDialog) {
        val pendingDebts = debts.filter { !it.isSettled }
        DebtPaymentDialog(
            pendingDebts = pendingDebts,
            onDismiss = { showDebtPaymentDialog = false },
            onSettleDebt = { debtId ->
                viewModel.settleDebt(debtId)
            }
        )
    }

    // Add New Product Dialog
    if (showNewProductDialog) {
        NewProductDialog(
            onDismiss = { showNewProductDialog = false },
            onConfirmProduct = { name, code, brandOrOrigin, sell, hpp, sav ->
                viewModel.createNewProduct(name, code, brandOrOrigin, sell, hpp, sav) {
                    showNewProductDialog = false
                }
            }
        )
    }

    // Sign Out Confirmation
    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Konfirmasi Keluar") },
            text = { Text("Apakah Anda yakin ingin keluar dari akun $userEmail?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DebtRed)
                ) {
                    Text("Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
