package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class ArthaStockRepository(val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Pengguna harus masuk menggunakan Google Sign-In.")
    }

    // --- USER PROFILES ---
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = runCatching {
        val uid = profile.uid.ifBlank { requireUserId() }
        val docRef = db.collection("users").document(uid)
        val payload = mapOf(
            "uid" to uid,
            "displayName" to profile.displayName,
            "email" to profile.email,
            "role" to profile.role,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload, SetOptions.merge()).await()
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val docRef = db.collection("users").document(userId)
        val subscription = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                handleFirestoreError(error, OperationType.READ, docRef.path)
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(UserProfile::class.java))
        }
        awaitClose { subscription.remove() }
    }

    fun observeAllUsers(): Flow<List<UserProfile>> = flow {
        val path = "users"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(UserProfile::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    // --- PRODUCTS ---
    fun observeProducts(): Flow<List<Product>> = flow {
        val path = "products"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Product::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createProduct(product: Product): Result<String> = runCatching {
        requireUserId()
        val colRef = db.collection("products")
        val payload = mapOf(
            "name" to product.name,
            "code" to product.code,
            "sellPrice" to product.sellPrice,
            "hppPrice" to product.hppPrice,
            "savingsPerUnit" to product.savingsPerUnit,
            "mandatoryDepositDefault" to (product.hppPrice + product.savingsPerUnit),
            "category" to product.category,
            "active" to product.active,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val doc = colRef.add(payload).await()
        doc.id
    }

    suspend fun updateProduct(product: Product): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("products").document(product.id)
        val payload = mapOf(
            "name" to product.name,
            "code" to product.code,
            "sellPrice" to product.sellPrice,
            "hppPrice" to product.hppPrice,
            "savingsPerUnit" to product.savingsPerUnit,
            "mandatoryDepositDefault" to (product.hppPrice + product.savingsPerUnit),
            "category" to product.category,
            "active" to product.active,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.update(payload).await()
    }

    // --- USER STOCK & ORIGIN BREAKDOWN ---
    fun observeUserStock(userId: String): Flow<List<UserStock>> = flow {
        val path = "user_stock"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(UserStock::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    // --- SALES TRANSACTIONS & CASH FLOW ---
    fun observeUserTransactions(userId: String): Flow<List<SaleTransaction>> = flow {
        val path = "transactions"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(SaleTransaction::class.java)
                        .sortedByDescending { it.timestamp?.seconds ?: 0L }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun recordSale(transaction: SaleTransaction): Result<String> = runCatching {
        val uid = requireUserId()
        val txCol = db.collection("transactions")
        val stockDocId = "${uid}_${transaction.productId}"
        val stockRef = db.collection("user_stock").document(stockDocId)

        // Atomic batch update for stock deduction and transaction recording
        db.runBatch { batch ->
            // Insert sale transaction
            val txDocRef = txCol.document()
            val txPayload = mapOf(
                "userId" to uid,
                "userName" to transaction.userName,
                "productId" to transaction.productId,
                "productName" to transaction.productName,
                "quantity" to transaction.quantity,
                "sellPricePerUnit" to transaction.sellPricePerUnit,
                "hppPerUnit" to transaction.hppPerUnit,
                "savingsPerUnit" to transaction.savingsPerUnit,
                "isSavingsIncluded" to transaction.isSavingsIncluded,
                "totalRevenue" to transaction.totalRevenue,
                "totalHpp" to transaction.totalHpp,
                "totalSavings" to transaction.totalSavings,
                "totalMandatoryDeposit" to transaction.totalMandatoryDeposit,
                "netMarginSales" to transaction.netMarginSales,
                "isDepositSettled" to transaction.isDepositSettled,
                "timestamp" to FieldValue.serverTimestamp()
            )
            batch.set(txDocRef, txPayload)

            // Deduct available stock & increment sold count
            batch.set(
                stockRef,
                mapOf(
                    "userId" to uid,
                    "productId" to transaction.productId,
                    "productName" to transaction.productName,
                    "totalAvailableStock" to FieldValue.increment(-transaction.quantity),
                    "soldCount" to FieldValue.increment(transaction.quantity),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
        }.await()

        "SUCCESS"
    }

    // --- WEEKLY POOL DISTRIBUTION & ROLL-OVER ---
    suspend fun distributeWeeklyPool(
        pool: WeeklyPool,
        allocations: Map<String, Long> // map of userId to allocated quantity
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val poolCol = db.collection("weekly_pools")
        val poolDocRef = poolCol.document()

        db.runBatch { batch ->
            // 1. Create WeeklyPool batch record
            val poolPayload = mapOf(
                "weekIdentifier" to pool.weekIdentifier,
                "productId" to pool.productId,
                "productName" to pool.productName,
                "totalPoolQty" to pool.totalPoolQty,
                "allocatedQty" to pool.allocatedQty,
                "createdBy" to uid,
                "createdAt" to FieldValue.serverTimestamp()
            )
            batch.set(poolDocRef, poolPayload)

            // 2. Update each user's stock ledger with origin rollover
            allocations.forEach { (targetUserId, qty) ->
                if (qty > 0) {
                    val stockDocRef = db.collection("user_stock").document("${targetUserId}_${pool.productId}")
                    // Add new allocation to available stock and log this week's allocation
                    batch.set(
                        stockDocRef,
                        mapOf(
                            "userId" to targetUserId,
                            "productId" to pool.productId,
                            "productName" to pool.productName,
                            "totalAvailableStock" to FieldValue.increment(qty),
                            "currentWeekAllocation" to FieldValue.increment(qty),
                            "updatedAt" to FieldValue.serverTimestamp()
                        ),
                        SetOptions.merge()
                    )
                }
            }
        }.await()

        poolDocRef.id
    }

    suspend fun closeWeeklyCycleAndRollover(userId: String, productId: String): Result<Unit> = runCatching {
        val stockRef = db.collection("user_stock").document("${userId}_${productId}")
        val snapshot = stockRef.get().await()
        if (snapshot.exists()) {
            val currentStock = snapshot.getLong("totalAvailableStock") ?: 0L
            // Rollover current stock to rolloverFromLastWeek and reset current week allocation
            stockRef.set(
                mapOf(
                    "rolloverFromLastWeek" to currentStock,
                    "currentWeekAllocation" to 0L,
                    "soldCount" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
        }
    }

    // --- PEER TRANSFERS ---
    fun observeTransfers(userId: String): Flow<List<TransferRecord>> = flow {
        val path = "transfers"
        emitAll(
            db.collection(path)
                .whereEqualTo("fromUserId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(TransferRecord::class.java)
                        .sortedByDescending { it.timestamp?.seconds ?: 0L }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun transferStock(transfer: TransferRecord): Result<String> = runCatching {
        val fromUid = requireUserId()
        val toUid = transfer.toUserId
        require(toUid.isNotBlank() && toUid != fromUid) { "Penerima transfer tidak valid." }
        require(transfer.quantity > 0) { "Jumlah transfer harus lebih dari 0." }

        val transferCol = db.collection("transfers")
        val transferDocRef = transferCol.document()
        val senderStockRef = db.collection("user_stock").document("${fromUid}_${transfer.productId}")
        val receiverStockRef = db.collection("user_stock").document("${toUid}_${transfer.productId}")

        db.runBatch { batch ->
            // Record transfer event
            val transferPayload = mapOf(
                "fromUserId" to fromUid,
                "fromUserName" to transfer.fromUserName,
                "toUserId" to toUid,
                "toUserName" to transfer.toUserName,
                "productId" to transfer.productId,
                "productName" to transfer.productName,
                "quantity" to transfer.quantity,
                "note" to transfer.note,
                "timestamp" to FieldValue.serverTimestamp()
            )
            batch.set(transferDocRef, transferPayload)

            // Deduct from sender
            batch.set(
                senderStockRef,
                mapOf(
                    "userId" to fromUid,
                    "productId" to transfer.productId,
                    "productName" to transfer.productName,
                    "totalAvailableStock" to FieldValue.increment(-transfer.quantity),
                    "transfersSent" to FieldValue.increment(transfer.quantity),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )

            // Add to receiver
            batch.set(
                receiverStockRef,
                mapOf(
                    "userId" to toUid,
                    "productId" to transfer.productId,
                    "productName" to transfer.productName,
                    "totalAvailableStock" to FieldValue.increment(transfer.quantity),
                    "transfersReceived" to FieldValue.increment(transfer.quantity),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
        }.await()

        transferDocRef.id
    }

    // --- DEBTS & CONSIGNMENT / SETORAN TERTUNDA ---
    fun observeDebts(userId: String): Flow<List<DebtRecord>> = flow {
        val path = "debts"
        emitAll(
            db.collection(path)
                .whereEqualTo("userId", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(DebtRecord::class.java)
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createDebtRecord(debt: DebtRecord): Result<String> = runCatching {
        val uid = requireUserId()
        val docRef = db.collection("debts").document()
        val payload = mapOf(
            "userId" to uid,
            "userName" to debt.userName,
            "type" to debt.type,
            "productId" to debt.productId,
            "productName" to debt.productName,
            "quantity" to debt.quantity,
            "totalNominal" to debt.totalNominal,
            "isSettled" to false,
            "settledAt" to null,
            "createdAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()

        // If it was borrowed goods, increase user's available stock and log debtBorrowed
        if (debt.type == "PINJAM_BARANG" && debt.quantity > 0 && debt.productId.isNotBlank()) {
            val stockRef = db.collection("user_stock").document("${uid}_${debt.productId}")
            stockRef.set(
                mapOf(
                    "userId" to uid,
                    "productId" to debt.productId,
                    "productName" to debt.productName,
                    "totalAvailableStock" to FieldValue.increment(debt.quantity),
                    "debtBorrowed" to FieldValue.increment(debt.quantity),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            ).await()
        }

        docRef.id
    }

    suspend fun settleDebt(debtId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("debts").document(debtId)
        docRef.update(
            mapOf(
                "isSettled" to true,
                "settledAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}
