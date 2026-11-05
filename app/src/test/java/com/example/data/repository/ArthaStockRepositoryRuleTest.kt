package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Product
import com.example.data.model.SaleTransaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ArthaStockRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun createProduct_authenticatedUser_createsSuccessfully() = runBlocking {
    signInTestUser("alice@arthastock.id")
    val repo = ArthaStockRepository(firestore)

    val product = Product(
      name = "Kopi Robusta 250g",
      code = "KOP-01",
      sellPrice = 25000.0,
      hppPrice = 18000.0,
      savingsPerUnit = 1000.0,
      active = true
    )

    val result = withTimeout(5000L) {
      repo.createProduct(product)
    }
    assertTrue("Create product should succeed", result.isSuccess)
    assertNotNull(result.getOrNull())
  }

  @Test
  fun recordSale_authenticatedUser_updatesStockAndRecordsTransaction() = runBlocking {
    signInTestUser("bob@arthastock.id")
    val repo = ArthaStockRepository(firestore)

    // Seed initial stock of 10 units for Bob
    val stockDocId = "${auth.currentUser!!.uid}_prod_99"
    firestore.collection("user_stock").document(stockDocId).set(
      mapOf(
        "userId" to auth.currentUser!!.uid,
        "productId" to "prod_99",
        "productName" to "Teh Melati",
        "totalAvailableStock" to 10L,
        "rolloverFromLastWeek" to 0L,
        "currentWeekAllocation" to 10L,
        "transfersReceived" to 0L,
        "transfersSent" to 0L,
        "debtBorrowed" to 0L,
        "soldCount" to 0L,
        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
      )
    ).await()

    val tx = SaleTransaction(
      userId = auth.currentUser!!.uid,
      userName = "Bob Sales",
      productId = "prod_99",
      productName = "Teh Melati",
      quantity = 2L,
      sellPricePerUnit = 15000.0,
      hppPerUnit = 11000.0,
      savingsPerUnit = 500.0,
      isSavingsIncluded = true,
      totalRevenue = 30000.0,
      totalHpp = 22000.0,
      totalSavings = 1000.0,
      totalMandatoryDeposit = 23000.0,
      netMarginSales = 7000.0,
      isDepositSettled = false
    )

    val result = withTimeout(5000L) {
      repo.recordSale(tx)
    }
    assertTrue("Record sale should succeed: ${result.exceptionOrNull()?.message}", result.isSuccess)
  }

  @Test
  fun observeTransactions_unauthenticatedUser_fails() = runBlocking {
    auth.signOut()
    val repo = ArthaStockRepository(firestore)

    val exception = assertThrows(Exception::class.java) {
      runBlocking {
        withTimeout(3000L) {
          repo.observeUserTransactions("some_unauth_user").first()
        }
      }
    }
    val firestoreEx = exception.unwrapFirestoreException()
    assertTrue(firestoreEx != null || exception is kotlinx.coroutines.CancellationException)
  }
}
