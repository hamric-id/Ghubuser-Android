package com.hamric.feature.detail_user

import com.hamric.domain.model.DetailUser

data class DetailUserUiState(
    val user: DetailUser? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)