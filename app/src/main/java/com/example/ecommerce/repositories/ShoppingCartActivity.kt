package com.example.ecommerce.repositories

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.ecommerce.models.CartItem
import com.example.ecommerce.ui.Charcoal
import com.example.ecommerce.ui.CharcoalLight
import com.example.ecommerce.ui.Cream
import com.example.ecommerce.ui.CreamDark
import com.example.ecommerce.ui.EbonyLight
import com.example.ecommerce.ui.Montserrat
import com.example.ecommerce.ui.MyApplicationTheme
import com.example.ecommerce.ui.Sage
import com.example.ecommerce.ui.SageLight
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import java.util.LinkedList
import java.util.UUID

class ShoppingCartActivity : ComponentActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoppingCartScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ShoppingCartScreen() {
        val cartItemsState = remember { mutableStateOf(LinkedList<CartItem>()) }
        val cartItems: LinkedList<CartItem> = cartItemsState.value
        val allItemsState = remember { mutableStateOf<List<CartItem>>(emptyList()) }
        var isCartOpen by remember { mutableStateOf(false) }
        var usersList by remember { mutableStateOf<List<String>>(emptyList()) }
        var receiverEmail by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            FirebaseFirestore.getInstance().collection("Articles")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val fetched = snapshot.mapNotNull { doc ->
                            val name = doc.getString("name") ?: return@mapNotNull null
                            val price = doc.getDouble("price") ?: 0.0
                            val qtyInStock = doc.getLong("quantityInStock")?.toInt() ?: 0
                            val imageUrl = doc.getString("imageUrl") ?: ""
                            CartItem(name, price, qtyInStock, imageUrl)
                        }
                        allItemsState.value = fetched
                    } else {
                        Log.d("ShoppingCart", "No articles found in Firestore (Articles).")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ShoppingCart", "Error fetching from 'Articles': $e")
                }
        }

        val currentUserId = firebaseAuth.currentUser?.uid ?: ""
        LaunchedEffect(Unit) {
            FirebaseFirestore.getInstance().collection("users")
                .whereNotEqualTo("uid", currentUserId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        usersList = snapshot.mapNotNull { doc ->
                            doc.getString("email")
                        }
                        Log.d("ShoppingCart", "Fetched user emails: $usersList")
                    } else {
                        Log.d("ShoppingCart", "No users in 'User' collection.")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ShoppingCart", "Error fetching users: $e")
                }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EbonyLight),
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,

                ) {
                Text(
                    text = "Articles for Sale",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold,
                    color = Cream,
                    fontSize = 32.sp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(allItemsState.value) { article ->
                        ArticleItem(
                            article = article,
                            onAddToCart = {
                                val newList = LinkedList(cartItems)
                                newList.add(article)
                                cartItemsState.value = newList

                                Toast.makeText(
                                    this@ShoppingCartActivity,
                                    "${article.name} added to cart",

                                    Toast.LENGTH_SHORT

                                ).show()
                            }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { isCartOpen = true },
                containerColor = SageLight,
                contentColor = Cream,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            ) {
                Text("Cart")
            }

            if (isCartOpen) {
                ModalBottomSheet(
                    onDismissRequest = { isCartOpen = false }
                ) {
                    CartBottomSheetContent(
                        cartItems = cartItems,
                        receiverEmail = receiverEmail,
                        usersList = usersList,
                        onSelectEmail = { receiverEmail = it },
                        onShareCart = { items, recvEmail ->
                            if (recvEmail.isNotBlank()) {
                                shareCart(items, recvEmail)
                            } else {
                                Toast.makeText(
                                    this@ShoppingCartActivity,
                                    "Pick a user to share with",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onClose = { isCartOpen = false }
                    )
                }
            }
        }
    }


    @Composable
    fun CartBottomSheetContent(
        cartItems: LinkedList<CartItem>,
        receiverEmail: String,
        usersList: List<String>,
        onSelectEmail: (String) -> Unit,
        onShareCart: (List<CartItem>, String) -> Unit,
        onClose: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CharcoalLight)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Your Cart",
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                color = Cream,
                fontSize = 26.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (cartItems.isEmpty()) {
                Text(
                    "Cart is empty.",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 460.dp)
                ) {
                    items(cartItems.toList()) { cartItem ->
                        CartItemView(cartItem = cartItem)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Pick a user to share with:",
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.heightIn(max = 270.dp)
            ) {
                items(usersList) { email ->
                    TextButton(
                        onClick = {
                            onSelectEmail(email)
                            Toast.makeText(
                                this@ShoppingCartActivity,
                                "$email selected",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Text(email)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Sage,
                    contentColor = Cream
                ),
                onClick = { onShareCart(cartItems.toList(), receiverEmail) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Share Cart with $receiverEmail",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    color = Cream, fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(border = BorderStroke(2.dp, Sage),
                onClick = { onClose() }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Close Cart",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    color = Cream,
                    fontSize = 18.sp
                )
            }
        }
    }

    @Composable
    fun ArticleItem(article: CartItem, onAddToCart: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberImagePainter(article.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.name,
                    style = MaterialTheme.typography.bodyLarge.copy(color = CreamDark)
                )
                Text(
                    "Price: ${article.price}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = CreamDark)
                )
                Text(
                    "Stock: ${article.quantity}",
                    style = MaterialTheme.typography.bodySmall.copy(color = CreamDark)
                )
            }
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Sage,
                    contentColor = Cream
                ), onClick = onAddToCart
            ) {
                Text("Add to Cart")
            }
        }
    }

    // CartItem display
    @Composable
    fun CartItemView(cartItem: CartItem) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberImagePainter(cartItem.imageUrl),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cartItem.name, style = MaterialTheme.typography.bodyMedium)
                Text("Price: ${cartItem.price}", style = MaterialTheme.typography.bodySmall)
                Text("Qty: ${cartItem.quantity}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    private fun shareCart(cartItems: List<CartItem>, receiverEmail: String) {
        val db = FirebaseFirestore.getInstance()

        val currentUserId = firebaseAuth.currentUser?.uid ?: ""
        if (currentUserId.isBlank()) {
            Toast.makeText(this, "You must be logged in to share a cart", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .whereEqualTo("email", receiverEmail)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(this, "No user found with $receiverEmail", Toast.LENGTH_SHORT)
                        .show()
                    return@addOnSuccessListener
                }

                val receiverUserId = snapshot.documents.first().id
                Log.d("ShoppingCart", "receiverUserId = $receiverUserId")

                val cartItemsData = cartItems.map {
                    mapOf(
                        "name" to it.name,
                        "price" to it.price,
                        "quantity" to it.quantity,
                        "imageUrl" to it.imageUrl
                    )
                }

                val cartData = hashMapOf(
                    "CartId" to UUID.randomUUID().toString(),
                    "ReceivedUserId" to receiverUserId,
                    "SharedUserId" to currentUserId,
                    "items" to cartItemsData
                )

                db.collection("SharedCarts").add(cartData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cart shared successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error adding doc to SharedCarts: $e", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to find receiver: $e", Toast.LENGTH_SHORT).show()
            }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewShoppingCartScreen() {
        ShoppingCartScreen()
    }
}
