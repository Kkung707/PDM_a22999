package com.example.ecommerce.ui.Article

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.ecommerce.ui.Montserrat
import com.google.firebase.firestore.FirebaseFirestore

data class ArticleUI(
    val docId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageArticlesScreen() {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    val articles = remember { mutableStateListOf<ArticleUI>() }

    var editDocId by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    fun resetForm() {
        name = ""
        price = ""
        quantity = ""
        imageUrl = ""
        editDocId = null
    }

    LaunchedEffect(Unit) {
        firestore.collection("Articles")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val price = doc.getDouble("price") ?: 0.0
                        val qty = doc.getLong("quantityInStock")?.toInt() ?: 0
                        val imageUrl = doc.getString("imageUrl") ?: ""
                        ArticleUI(
                            docId = doc.id,
                            name = name,
                            price = price,
                            quantity = qty,
                            imageUrl = imageUrl
                        )
                    }
                    articles.clear()
                    articles.addAll(list)
                } else {
                    articles.clear()
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "Manage Articles",
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Article Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Price") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity in Stock") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        val isEditing = (editDocId != null)

        Button(
            onClick = {
                val parsedPrice = price.toDoubleOrNull() ?: 0.0
                val parsedQty = quantity.toIntOrNull() ?: 0

                if (isEditing) {
                    val docId = editDocId ?: return@Button
                    val updates = mapOf(
                        "name" to name,
                        "price" to parsedPrice,
                        "quantityInStock" to parsedQty,
                        "imageUrl" to imageUrl
                    )
                    firestore.collection("Articles")
                        .document(docId)
                        .update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Article updated!", Toast.LENGTH_SHORT).show()
                            resetForm()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: $e", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    val data = mapOf(
                        "name" to name,
                        "price" to parsedPrice,
                        "quantityInStock" to parsedQty,
                        "imageUrl" to imageUrl
                    )
                    firestore.collection("Articles")
                        .add(data)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Article added!", Toast.LENGTH_SHORT).show()
                            resetForm()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: $e", Toast.LENGTH_SHORT).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isEditing) "Update Article" else "Add Article")
        }

        if (isEditing) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { resetForm() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel Edit")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Divider()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Existing Articles:",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(articles) { article ->
                ManageArticleRow(
                    article = article,
                    onEdit = {
                        editDocId = article.docId
                        name = article.name
                        price = article.price.toString()
                        quantity = article.quantity.toString()
                        imageUrl = article.imageUrl
                    },
                    onDelete = {
                        firestore.collection("Articles")
                            .document(article.docId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Article deleted!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error: $e", Toast.LENGTH_SHORT).show()
                            }
                    }
                )
            }
        }
    }
}

@Composable
fun ManageArticleRow(
    article: ArticleUI,
    onEdit: (ArticleUI) -> Unit,
    onDelete: (ArticleUI) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberImagePainter(article.imageUrl),
                contentDescription = article.name,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Name: ${article.name}", style = MaterialTheme.typography.bodyLarge)
                Text("Price: \$${article.price}", style = MaterialTheme.typography.bodyMedium)
                Text("Qty: ${article.quantity}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Row {
            OutlinedButton(onClick = { onEdit(article) }) {
                Text("Edit")
            }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = { onDelete(article) }) {
                Text("Delete")
            }
        }
    }
}