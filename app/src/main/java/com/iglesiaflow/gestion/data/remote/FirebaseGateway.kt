package com.iglesiaflow.gestion.data.remote

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Puerta de entrada a Firebase (Auth, Firestore y FCM).
 *
 * La app es offline-first: si la iglesia no ha añadido su `google-services.json`
 * todas las operaciones se convierten en no-ops y la aplicación sigue funcionando
 * al 100% con Room. Ver docs/FIREBASE.md.
 */
@Singleton
class FirebaseGateway @Inject constructor(@ApplicationContext private val context: Context) {

    val isAvailable: Boolean by lazy {
        runCatching { FirebaseApp.getApps(context).isNotEmpty() }.getOrDefault(false)
    }

    suspend fun upsert(collection: String, documentId: String, data: Map<String, Any?>): Boolean {
        if (!isAvailable) return false
        return runCatching {
            Firebase.firestore.collection(collection).document(documentId).set(data).await()
            true
        }.getOrElse {
            Log.w(TAG, "No se pudo sincronizar $collection/$documentId", it)
            false
        }
    }

    suspend fun fetchAll(collection: String): List<Map<String, Any?>> {
        if (!isAvailable) return emptyList()
        return runCatching {
            Firebase.firestore.collection(collection).get().await().documents.map { it.data.orEmpty() }
        }.getOrDefault(emptyList())
    }

    suspend fun subscribeToTopic(topic: String): Boolean {
        if (!isAvailable) return false
        return runCatching { Firebase.messaging.subscribeToTopic(topic).await(); true }.getOrDefault(false)
    }

    suspend fun currentToken(): String? {
        if (!isAvailable) return null
        return runCatching { Firebase.messaging.token.await() }.getOrNull()
    }

    companion object { private const val TAG = "FirebaseGateway" }
}
