package com.hamric.feature.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hamric.core.common.dpToPx
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    navController: NavController,
    viewModel: UsersViewModel = koinViewModel()
) {
    val state by viewModel.state.observeAsState(UsersUiState())
    val login by viewModel.navigation.observeAsState()
    val searchResultTotalCount by viewModel.searchResultTotalCount.observeAsState()

    LaunchedEffect(login) {
        login?.let {
            navController.navigate("detail/$it")
            viewModel.onNavigationHandled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.systemBars)
    ) {
        Box {
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.onQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(com.hamric.feature.users.R.string.search_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )
            if (state.isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 40.dp)
                        .size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        searchResultTotalCount?.let{
            Text(
                text = "$it users found",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 2.dp),
                textAlign = TextAlign.Left
            )
        }

        if (state.error != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Result showed from cache because ${state.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Button(onClick = { viewModel.retry() }) {
                    Text(stringResource(R.string.retry))
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            UsersRecyclerView(
                state = state,
                onUserClick = { viewModel.onUserClicked(it.login) },
                onScrolledToEnd = {
                    when (state.mode) {
                        UsersUiState.Mode.LIST -> viewModel.onListScrolledToEnd()
                        UsersUiState.Mode.SEARCH -> viewModel.onSearchScrolledToEnd()
                    }
                }
            )

            if (state.isEmpty && !state.isLoadingMore) {
                Text(
                    text = "No users found",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }

            if (state.isLoadingMore) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun UsersRecyclerView(
    state: UsersUiState,
    onUserClick: (com.hamric.domain.model.User) -> Unit,
    onScrolledToEnd: () -> Unit
) {
    val context = LocalContext.current
    val adapter = remember {
        UsersAdapter(onClick = onUserClick)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            RecyclerView(ctx).apply {
                layoutManager = LinearLayoutManager(ctx)
                this.adapter = adapter
                clipToPadding = false
                clipToOutline = true
                setPadding(0, 12.dpToPx(ctx), 0, 72.dpToPx(ctx))
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                        if (dy <= 0) return
                        val lm = rv.layoutManager as? LinearLayoutManager ?: return
                        val lastVisible = lm.findLastVisibleItemPosition()
                        val total = lm.itemCount
                        if (total == 0) return
                        if (lastVisible >= total - 3) onScrolledToEnd()
                    }
                })
            }
        },
        update = { rv ->
            (rv.adapter as? UsersAdapter)?.submitList(state.users)
        }
    )
}

