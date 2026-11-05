package com.example.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException

enum class OperationType {
    CREATE, READ, UPDATE, DELETE, LIST
}

fun handleFirestoreError(
    exception: Exception,
    operationType: OperationType,
    path: String
) {
    val firestoreException = exception as? FirebaseFirestoreException
    val errorCode = firestoreException?.code?.name ?: "UNKNOWN"
    val message = exception.localizedMessage ?: "Unknown Firestore error"
    
    Log.e(
        "ArthaStockFirestore",
        "{\"status\":\"error\",\"operation\":\"$operationType\",\"path\":\"$path\",\"code\":\"$errorCode\",\"message\":\"$message\"}",
        exception
    )
}
