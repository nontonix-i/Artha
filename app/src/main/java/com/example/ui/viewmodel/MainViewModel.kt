package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ArthaStockRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FinancialMetrics(
    val totalRevenue: Double = 0.0,
    val totalMandatoryDeposit: Double = 0.0,
    val pendingMandatoryDeposit: Double = 0.0,
    val settledMandatoryDeposit: Double = 0.0,
    val pendingDepositCount: Int = 0,
    val totalSavings: Double = 0.0, // Saldo tabungan aktif (penjualan + setor mandiri - tarik)
    val savingsFromSales: Double = 0.0,
    val savingsManualAdded: Double = 0.0,
    val savingsWithdrawn: Double = 0.0,
    val totalNetProfit: Double = 0.0,
    val pendingDebtNominal: Double = 0.0,
    val pendingDebtCount: Int = 0,
    val totalAvailableStock: Long = 0L
)

class MainViewModel(
    val repository: ArthaStockRepository,
    val currentUserId: String,
    val currentUserName: String
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.observeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val userStock: StateFlow<List<UserStock>> = repository.observeUserStock(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val transactions: StateFlow<List<SaleTransaction>> = repository.observeUserTransactions(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val transfers: StateFlow<List<TransferRecord>> = repository.observeTransfers(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val debts: StateFlow<List<DebtRecord>> = repository.observeDebts(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val savingsRecords: StateFlow<List<SavingsRecord>> = repository.observeSavingsRecords(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val teamUsers: StateFlow<List<UserProfile>> = repository.observeAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val financialMetrics: StateFlow<FinancialMetrics> = combine(
        transactions,
        debts,
        savingsRecords,
        userStock
    ) { txList, debtList, savingsList, stockList ->
        val rev = txList.sumOf { it.totalRevenue }
        val deposit = txList.sumOf { it.totalMandatoryDeposit }
        val pendingTxs = txList.filter { !it.isDepositSettled }
        val pendingDeposit = pendingTxs.sumOf { it.totalMandatoryDeposit }
        val settledDeposit = txList.filter { it.isDepositSettled }.sumOf { it.totalMandatoryDeposit }

        val savFromSales = txList.sumOf { it.totalSavings }
        val savManual = savingsList.filter { it.type == "SETOR_MANDIRI" }.sumOf { it.nominal }
        val savWithdrawn = savingsList.filter { it.type == "TARIK" }.sumOf { it.nominal }
        val netSavings = (savFromSales + savManual - savWithdrawn).coerceAtLeast(0.0)

        val profit = txList.sumOf { it.netMarginSales }
        val pendingDebts = debtList.filter { !it.isSettled }
        val debtNominal = pendingDebts.sumOf { it.totalNominal }
        val availableStockSum = stockList.sumOf { it.totalAvailableStock }

        FinancialMetrics(
            totalRevenue = rev,
            totalMandatoryDeposit = deposit,
            pendingMandatoryDeposit = pendingDeposit,
            settledMandatoryDeposit = settledDeposit,
            pendingDepositCount = pendingTxs.size,
            totalSavings = netSavings,
            savingsFromSales = savFromSales,
            savingsManualAdded = savManual,
            savingsWithdrawn = savWithdrawn,
            totalNetProfit = profit,
            pendingDebtNominal = debtNominal,
            pendingDebtCount = pendingDebts.size,
            totalAvailableStock = availableStockSum
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), FinancialMetrics())

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun recordSale(
        product: Product,
        quantity: Long,
        isSavingsIncluded: Boolean,
        note: String = "",
        onSuccess: () -> Unit
    ) {
        if (quantity <= 0) return
        _isActionLoading.value = true
        viewModelScope.launch {
            val hpp = product.hppPrice
            val savings = if (isSavingsIncluded) product.savingsPerUnit else 0.0
            val depositPerUnit = hpp + savings
            val totalRev = product.sellPrice * quantity
            val totalHppVal = hpp * quantity
            val totalSavVal = savings * quantity
            val totalDepositVal = depositPerUnit * quantity
            val netMargin = totalRev - totalDepositVal

            val tx = SaleTransaction(
                userId = currentUserId,
                userName = currentUserName,
                productId = product.id,
                productName = product.name,
                brandOrOrigin = product.brandOrOrigin,
                quantity = quantity,
                sellPricePerUnit = product.sellPrice,
                hppPerUnit = hpp,
                savingsPerUnit = savings,
                isSavingsIncluded = isSavingsIncluded,
                totalRevenue = totalRev,
                totalHpp = totalHppVal,
                totalSavings = totalSavVal,
                totalMandatoryDeposit = totalDepositVal,
                netMarginSales = netMargin,
                isDepositSettled = false,
                note = note
            )

            val result = repository.recordSale(tx)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Penjualan dicatat! Setor wajib: Rp %,.0f".format(totalDepositVal)
                onSuccess()
            } else {
                _statusMessage.value = "Gagal mencatat penjualan: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    // --- ALUR BAYAR SETORAN WAJIB ---
    fun settleDeposit(transactionId: String, onSuccess: () -> Unit = {}) {
        _isActionLoading.value = true
        viewModelScope.launch {
            val result = repository.settleDeposit(transactionId)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Setoran berhasil disetorkan ke kas pabrik!"
                onSuccess()
            } else {
                _statusMessage.value = "Gagal bayar setoran: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun settleAllPendingDeposits(onSuccess: () -> Unit = {}) {
        val pendingTxIds = transactions.value.filter { !it.isDepositSettled }.map { it.id }
        settleDepositsList(pendingTxIds, onSuccess)
    }

    fun settleDepositsList(transactionIds: List<String>, onSuccess: () -> Unit = {}) {
        _isActionLoading.value = true
        viewModelScope.launch {
            if (transactionIds.isEmpty()) {
                _isActionLoading.value = false
                _statusMessage.value = "Tidak ada setoran tertunda."
                return@launch
            }
            val result = repository.settleAllDeposits(transactionIds)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Sebanyak ${transactionIds.size} setoran berhasil disetorkan!"
                onSuccess()
            } else {
                _statusMessage.value = "Gagal menyetor: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    // --- ALUR TABUNGAN: TAMBAH LANGSUNG (NOMINAL MANDIRI) & AMBIL TABUNGAN ---
    fun addManualSavings(nominal: Double, note: String, onSuccess: () -> Unit) {
        if (nominal <= 0) return
        _isActionLoading.value = true
        viewModelScope.launch {
            val record = SavingsRecord(
                userId = currentUserId,
                userName = currentUserName,
                type = "SETOR_MANDIRI",
                nominal = nominal,
                note = note.ifBlank { "Nabung Mandiri" }
            )
            val result = repository.recordSavingsAction(record)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Berhasil menambah tabungan sebesar Rp %,.0f".format(nominal)
                onSuccess()
            } else {
                _statusMessage.value = "Gagal menambah tabungan: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun withdrawSavings(nominal: Double, note: String, onSuccess: () -> Unit) {
        if (nominal <= 0) return
        val currentBalance = financialMetrics.value.totalSavings
        if (nominal > currentBalance) {
            _statusMessage.value = "Saldo tabungan tidak mencukupi (Tersedia: Rp %,.0f)".format(currentBalance)
            return
        }

        _isActionLoading.value = true
        viewModelScope.launch {
            val record = SavingsRecord(
                userId = currentUserId,
                userName = currentUserName,
                type = "TARIK",
                nominal = nominal,
                note = note.ifBlank { "Penarikan Tabungan" }
            )
            val result = repository.recordSavingsAction(record)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Tabungan berhasil ditarik sebesar Rp %,.0f".format(nominal)
                onSuccess()
            } else {
                _statusMessage.value = "Gagal menarik tabungan: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    // --- ALUR TRANSFER ---
    fun transferGoods(
        toUser: UserProfile,
        product: Product,
        quantity: Long,
        note: String,
        onSuccess: () -> Unit
    ) {
        if (quantity <= 0) return
        _isActionLoading.value = true
        viewModelScope.launch {
            val transfer = TransferRecord(
                fromUserId = currentUserId,
                fromUserName = currentUserName,
                toUserId = toUser.uid,
                toUserName = toUser.displayName.ifBlank { toUser.email },
                productId = product.id,
                productName = product.name,
                quantity = quantity,
                note = note
            )
            val result = repository.transferStock(transfer)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Berhasil transfer $quantity unit ke ${transfer.toUserName}"
                onSuccess()
            } else {
                _statusMessage.value = "Gagal transfer: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    // --- ALUR CATAT & BAYAR HUTANG ---
    fun recordDebt(
        type: String, // "SETORAN_TERTUNDA" or "PINJAM_BARANG"
        product: Product?,
        quantity: Long,
        totalNominal: Double,
        note: String = "",
        onSuccess: () -> Unit
    ) {
        _isActionLoading.value = true
        viewModelScope.launch {
            // Pinjam barang fisik dihitung berbasis harga HPP pabrik/modal, bukan harga jual
            val finalNominal = if (type == "PINJAM_BARANG" && product != null && quantity > 0) {
                product.hppPrice * quantity
            } else {
                totalNominal
            }

            val debt = DebtRecord(
                userId = currentUserId,
                userName = currentUserName,
                type = type,
                productId = product?.id ?: "",
                productName = product?.name ?: if (type == "SETORAN_TERTUNDA") "Setoran Kas Pabrik" else "Barang",
                brandOrOrigin = product?.brandOrOrigin ?: "",
                quantity = quantity,
                totalNominal = finalNominal,
                note = note,
                isSettled = false
            )
            val result = repository.createDebtRecord(debt)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Catatan hutang berhasil disimpan (Nominal: Rp %,.0f)".format(finalNominal)
                onSuccess()
            } else {
                _statusMessage.value = "Gagal menyimpan hutang: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun settleDebt(debtId: String, onSuccess: () -> Unit = {}) {
        _isActionLoading.value = true
        viewModelScope.launch {
            val result = repository.settleDebt(debtId)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Hutang/Pinjaman berhasil dilunasi!"
                onSuccess()
            } else {
                _statusMessage.value = "Gagal pelunasan: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    // --- ALUR POOL JATAH MINGGUAN ---
    fun distributePool(
        product: Product,
        weekIdentifier: String,
        totalPoolQty: Long,
        allocations: Map<String, Long>,
        onSuccess: () -> Unit
    ) {
        _isActionLoading.value = true
        viewModelScope.launch {
            val pool = WeeklyPool(
                weekIdentifier = weekIdentifier,
                productId = product.id,
                productName = product.name,
                brandOrOrigin = product.brandOrOrigin,
                totalPoolQty = totalPoolQty,
                allocatedQty = allocations.values.sum(),
                createdBy = currentUserId
            )
            val result = repository.distributeWeeklyPool(pool, allocations)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Jatah mingguan baru berhasil dibagikan dari pool!"
                onSuccess()
            } else {
                _statusMessage.value = "Gagal distribusi pool: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun createNewProduct(
        name: String,
        code: String,
        brandOrOrigin: String,
        sellPrice: Double,
        hppPrice: Double,
        savingsPerUnit: Double,
        onSuccess: () -> Unit
    ) {
        _isActionLoading.value = true
        viewModelScope.launch {
            val p = Product(
                name = name,
                code = code,
                brandOrOrigin = brandOrOrigin.ifBlank { "Pabrik Utama" },
                sellPrice = sellPrice,
                hppPrice = hppPrice,
                savingsPerUnit = savingsPerUnit,
                mandatoryDepositDefault = hppPrice + savingsPerUnit,
                active = true
            )
            val result = repository.createProduct(p)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Produk '$name' berhasil ditambahkan!"
                onSuccess()
            } else {
                _statusMessage.value = "Gagal tambah produk: ${result.exceptionOrNull()?.message}"
            }
        }
    }
}

class MainViewModelFactory(
    private val repository: ArthaStockRepository,
    private val currentUserId: String,
    private val currentUserName: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, currentUserId, currentUserName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
