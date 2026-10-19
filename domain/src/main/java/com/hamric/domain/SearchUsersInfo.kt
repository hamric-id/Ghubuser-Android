package com.hamric.domain

data class SearchUsersInfo(
    val totalCount: Int,
    val perPage: Int,
    val currentPage: Int
) {
    private fun lastPage(): Int{
        val accessibleTotal = minOf(totalCount, 1000)
        return if (accessibleTotal == 0) 0
        else (accessibleTotal + perPage - 1) / perPage
    }


    val isLastPage: Boolean get() = currentPage >= lastPage()
}