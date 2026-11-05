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
    val totalSavings: Double = 0.0,
    val totalNetProfit: Double = 0.0,
    val pendingDebtNominal: Double = 0.0,
    val pendingDebtCount: Int = 0
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

    val teamUsers: StateFlow<List<UserProfile>> = repository.observeAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val financialMetrics: StateFlow<FinancialMetrics> = combine(transactions, debts) { txList, debtList ->
        val rev = txList.sumOf { it.totalRevenue }
        val deposit = txList.sumOf { it.totalMandatoryDeposit }
        val savings = txList.sumOf { it.totalSavings }
        val profit = txList.sumOf { it.netMarginSales }
        val pendingDebts = debtList.filter { !it.isSettled }
        val debtNominal = pendingDebts.sumOf { it.totalNominal }
        FinancialMetrics(
            totalRevenue = rev,
            totalMandatoryDeposit = deposit,
            totalSavings = savings,
            totalNetProfit = profit,
            pendingDebtNominal = debtNominal,
            pendingDebtCount = pendingDebts.size
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
                isDepositSettled = false
            )

            val result = repository.recordSale(tx)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Penjualan berhasil dicatat! Setor wajib: Rp %,.0f".format(totalDepositVal)
                onSuccess()
            } else {
                _statusMessage.value = "Gagal mencatat penjualan: ${result.exceptionOrNull()?.message}"
            }
        }
    }

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

    fun recordDebt(
        type: String, // "SETORAN_TERTUNDA" or "PINJAM_BARANG"
        product: Product?,
        quantity: Long,
        totalNominal: Double,
        onSuccess: () -> Unit
    ) {
        _isActionLoading.value = true
        viewModelScope.launch {
            val debt = DebtRecord(
                userId = currentUserId,
                userName = currentUserName,
                type = type,
                productId = product?.id ?: "",
                productName = product?.name ?: if (type == "SETORAN_TERTUNDA") "Setoran Kas Pabrik" else "Barang",
                quantity = quantity,
                totalNominal = totalNominal,
                isSettled = false
            )
            val result = repository.createDebtRecord(debt)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Catatan hutang/pinjaman berhasil disimpan."
                onSuccess()
            } else {
                _statusMessage.value = "Gagal menyimpan hutang: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun settleDebt(debtId: String) {
        _isActionLoading.value = true
        viewModelScope.launch {
            val result = repository.settleDebt(debtId)
            _isActionLoading.value = false
            if (result.isSuccess) {
                _statusMessage.value = "Setoran/Hutang telah dilunasi!"
            } else {
                _statusMessage.value = "Gagal pelunasan: ${result.exceptionOrNull()?.message}"
            }
        }
    }

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
