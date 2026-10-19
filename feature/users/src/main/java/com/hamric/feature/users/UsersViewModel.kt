package com.hamric.feature.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hamric.core.common.Resource
import com.hamric.domain.repository.UsersRepository
import com.hamric.domain.usecase.FetchUsersUseCase
import com.hamric.domain.usecase.ObserveCachedUsersUseCase
import com.hamric.domain.usecase.ObserveAndFilterCachedUsersUseCase
import com.hamric.domain.usecase.SearchUsersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class UsersViewModel(
    private val observeCachedUsers: ObserveCachedUsersUseCase,
    private val fetchUsers: FetchUsersUseCase,
    private val searchCachedUsers: ObserveAndFilterCachedUsersUseCase,
    private val searchUsers: SearchUsersUseCase,
    private val usersRepository: UsersRepository
): ViewModel() {
    private val _state = MutableLiveData(UsersUiState())
    val state: LiveData<UsersUiState> = _state


    private val queryFlow = MutableStateFlow("")

    private var listSince = 0L
    private var lastFetchSearchPage = 1
    private val maxResultSearchPerPage = 20
    private val _searchResultTotalCount =MutableLiveData<Int?>(null)
    val searchResultTotalCount: LiveData<Int?> = _searchResultTotalCount
    private var currentIsLastSearchPage = false
    private var searchQueryForPaging = ""
    private var listEndReached = false
    private var searchEndReached = false
    private var observeJob: Job? = null
    private var searchJob: Job? = null
    private var initialFetchTriggered = false
    private val _navigation = MutableLiveData<String?>()
    val navigation : LiveData<String?> = _navigation

    fun onUserClicked(login: String){
        _navigation.value = login
    }

    fun onNavigationHandled(){
        _navigation.value = null
    }

    fun onQueryChange(newQuery: String) {
        val wasEmpty = _state.value?.query.isNullOrBlank()
        val isEmptyNow = newQuery.isBlank()

        updateState { it.copy(query = newQuery, error = null) }

        if (isEmptyNow) { // Switch to USER LIST mode
            if (!wasEmpty) {
                initialFetchTriggered = false
                listEndReached = false
                updateState {
                    it.copy(
                        mode = UsersUiState.Mode.LIST,
                        users = emptyList(),
                        isEmpty = false,
                        endReached = false
                    )
                }
                lastFetchSearchPage = 1
                currentIsLastSearchPage = false
                _searchResultTotalCount.value = null
                searchEndReached = false
                searchQueryForPaging = ""
            }
            enterListMode()
        } else { // Switch to SEARCH mode / stay
            if (wasEmpty) {
                updateState {
                    it.copy(
                        mode = UsersUiState.Mode.SEARCH,
                        users = emptyList(),
                        isEmpty = false,
                        endReached = false
                    )
                }
                listEndReached = false
                observeJob?.cancel()
            }
            queryFlow.value = newQuery
        }
    }

    fun retry(){
        val s = _state.value ?: return
        if (s.mode == UsersUiState.Mode.SEARCH && s.query.isNotBlank()){
            runSearch(s.query)
        }else{
            viewModelScope.launch { fetchNextUsersPage() }
        }
    }

    fun onListScrolledToEnd(){
        val s = _state.value?: return
        if(s.mode != UsersUiState.Mode.LIST ||
            s.isLoadingMore ||
            listEndReached
            ) return
        viewModelScope.launch { fetchNextUsersPage() }
    }

    fun onSearchScrolledToEnd(){
        val s = _state.value?: return
        if(s.mode != UsersUiState.Mode.SEARCH ||
            s.isLoadingMore ||
            searchEndReached ||
            searchQueryForPaging.isBlank()
            ) return
        viewModelScope.launch { fetchNextSearchPage() }
    }

    init{
        viewModelScope.launch {
            listSince = usersRepository.lastListSince()
        }

        enterListMode()
        startSearchDebounce()
    }

    private fun startSearchDebounce(){
        searchJob = viewModelScope.launch {
            queryFlow
                .debounce(1000)
                .distinctUntilChanged()
                .filter { it.isNotBlank() }
                .collect{q-> runSearch(q)}
        }
    }

    private fun runSearch(query: String){
        lastFetchSearchPage = 1
        _searchResultTotalCount.value = null
        currentIsLastSearchPage = false
        searchQueryForPaging = query
        searchEndReached = false

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            searchCachedUsers(query).collect { users ->
                if(_state.value?.mode == UsersUiState.Mode.SEARCH){
                    updateState{ lastUsersUiState->
                        lastUsersUiState.copy(
                            users = users,
                            isEmpty = users.isEmpty() &&
                                    !lastUsersUiState.isLoadingMore &&
                                    !lastUsersUiState.isSearching
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            updateState{ lastUsersUiState->
                lastUsersUiState.copy(
                    isSearching = true,
                    error = null
                )
            }

            when(val result = searchUsers(query, lastFetchSearchPage, maxResultSearchPerPage)){
                is Resource.Success -> {
                    currentIsLastSearchPage = result.data.isLastPage
                    _searchResultTotalCount.value = result.data.totalCount
                    updateState {
                        it.copy(isSearching = false)
                    }
                }
                is Resource.Error -> updateState {
                    it.copy(
                        isSearching = false,
                        error = result.throwable.message ?: "Bad Network — showing cached results"
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }

    private suspend fun fetchNextSearchPage(){
        if (_state.value?.isLoadingMore == true) return
        updateState { it.copy(isLoadingMore = true) }

        val nextPage = lastFetchSearchPage + 1
        when (val result = searchUsers(searchQueryForPaging, nextPage, maxResultSearchPerPage)) {
            is Resource.Success -> {
                lastFetchSearchPage = result.data.currentPage
                currentIsLastSearchPage = result.data.isLastPage
                _searchResultTotalCount.value = result.data.totalCount
                updateState { it.copy(isLoadingMore = false) }
            }
            is Resource.Error -> updateState {
                it.copy(
                    isLoadingMore = false,
                    error = result.throwable.message ?: "Couldn't load more results"
                )
            }
            Resource.Loading -> Unit
        }
    }

    private fun enterListMode(){
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeCachedUsers().collect { users->
                if(_state.value?.mode != UsersUiState.Mode.LIST) return@collect

                updateState{ lastUsersUiState->
                    lastUsersUiState.copy(
                        users = users,
                        isEmpty = users.isEmpty() && !lastUsersUiState.isLoadingMore && !listEndReached
                    )
                }

                if(!initialFetchTriggered && users.isEmpty() && !listEndReached){
                    initialFetchTriggered = true
                    fetchNextUsersPage()
                }
            }
        }
    }

    private suspend fun fetchNextUsersPage(){
        if (_state.value?.isLoadingMore == true) return

        updateState{ lastUsersUiState->
            lastUsersUiState.copy(
                isLoadingMore = true,
                error = null
            )
        }

        when(val result = fetchUsers(listSince)){
            is Resource.Success -> {
                val newSince = result.data
                val madeProgress = newSince != listSince
                listSince = newSince

                if(madeProgress){
                    listEndReached = true
                }

                updateState{ lastUsersUiState->
                    lastUsersUiState.copy(
                        isLoadingMore = false,
                        endReached = listEndReached
                    )
                }
            }
            is Resource.Error-> updateState{ lastUsersUiState->
                lastUsersUiState.copy(
                    isLoadingMore = false,
                    error = result.throwable.message ?: "Couldn't fetch users"
                )
            }
            Resource.Loading -> Unit
        }
    }

    private inline fun updateState(block: (UsersUiState) -> UsersUiState){
        val current = _state.value ?: UsersUiState()
        _state.value = block(current)
    }

}