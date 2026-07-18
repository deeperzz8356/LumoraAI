package com.deep.lumoraai.data.remote

import com.deep.lumoraai.data.model.PromptDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class PromptDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getPrompts(): Flow<List<PromptDto>> = callbackFlow {
        val listener = firestore.collection("prompts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val prompts = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PromptDto::class.java)
                    }
                    trySend(prompts)
                }
            }
            
        awaitClose { listener.remove() }
    }
}
