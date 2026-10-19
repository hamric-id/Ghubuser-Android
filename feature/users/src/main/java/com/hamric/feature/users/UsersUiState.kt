package com.hamric.feature.users

import com.hamric.domain.model.User

data class UsersUiState(
    val query: String = "",
    val users: List<User> = emptyList(),
    val mode: Mode = Mode.LIST,
    val isSearching: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val isEmpty: Boolean = false,
    val endReached: Boolean = false
){
    enum class Mode { LIST, SEARCH }
}