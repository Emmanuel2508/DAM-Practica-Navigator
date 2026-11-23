package com.example.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

// ---------- Datos de ejemplo ----------

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String
)

val sampleProducts = listOf(
    Product(1, "Quesadilla sencilla", 40.00, "Quesadilla con queso"),
    Product(2, "Quesadilla con guiso", 55.00, "Guiso a elegir"),
    Product(3, "Orden de flautas", 60.00, "Crema, salsa y repollo"),
    Product(4, "Swandich", 55.00, "Pierna o pollo "),
    Product(5, "Tacos con guiso", 35.00, "Orden de 3 tacos con guiso a elegir"),
)

// ---------- Claves de navegación ----------

@Serializable
data object Home : NavKey

@Serializable
data class Detail(val id: Int) : NavKey

// ---------- Navegación ----------

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun Navigation() {
    val backStack = rememberNavBackStack(Home)
    val listDetailStrategy = rememberListDetailSceneStrategy<Any>()

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(listDetailStrategy),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it / 3 })
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it / 3 }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        entryProvider = entryProvider {
            entry<Home>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Elige un producto de la lista")
                            }
                        }
                    }
                )
            ) {
                HomeScreen(
                    onProductClick = { id ->
                        if (backStack.lastOrNull() is Detail) {
                            backStack.removeLastOrNull()
                        }
                        backStack.add(Detail(id))
                    }
                )
            }
            entry<Detail>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                DetailScreen(
                    id = key.id,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

// ---------- Pantallas ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val products by viewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Menu") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProductClick(product.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${product.price}",
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    id: Int,
    onBack: () -> Unit,
    viewModel: DetailViewModel = viewModel(
        factory = viewModelFactory { initializer { DetailViewModel(id) } }
    )
) {
    val product = viewModel.product

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Producto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$${product.price}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Descripción",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(text = product.description)
        }
    }
}