package com.hamric.feature.detail_user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi

@Composable
fun DetailUserScreen(
    login: String,
    viewModel: DetailUserViewModel = koinViewModel()
) {
    val state by viewModel.state.observeAsState(DetailUserUiState())

    LaunchedEffect(login) {
        viewModel.load(login)
    }

    DetailUserContent(
        state = state,
        onRetry = viewModel::retry
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DetailUserContent(
    state: DetailUserUiState,
    onRetry: () -> Unit
) {
    val user = state.user

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (user != null) {
                GlideImage(
                    model = user.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    loading = placeholder(android.R.color.darker_gray)
                )

                Spacer(Modifier.height(12.dp))
                Text(
                    text = user.name ?: user.login,
                    fontSize = 22.sp,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "@${user.login}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (!user.bio.isNullOrBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = user.bio ?: "",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = stringResource(R.string.label_repos),
                        value = user.publicRepos.toString()
                    )
                    StatItem(
                        label = stringResource(R.string.label_followers),
                        value = user.followers.toString()
                    )
                    StatItem(
                        label = stringResource(R.string.label_following),
                        value = user.following.toString()
                    )
                }

                Spacer(Modifier.height(20.dp))
                Column(modifier = Modifier.fillMaxWidth()) {
                    MetaItem(emoji = "\uD83C\uDFE2", value = user.company)
                    MetaItem(emoji = "\uD83D\uDCCD", value = user.location)
                    MetaItem(emoji = "\uD83D\uDD17", value = user.blog)
                    MetaItem(
                        emoji = "\uD83D\uDC26",
                        value = user.twitterUsername?.let { "@$it" }
                    )
                }
            }

            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }

        if (state.isLoading && user == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            style = MaterialTheme.typography.titleMedium
        )
        Text(text = label, fontSize = 12.sp)
    }
}

@Composable
private fun MetaItem(emoji: String, value: String?) {
    if (value.isNullOrBlank()) return
    Text(
        text = "$emoji $value",
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    )
}