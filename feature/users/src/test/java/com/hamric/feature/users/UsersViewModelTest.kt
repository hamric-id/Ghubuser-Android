package com.hamric.feature.users

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hamric.core.common.Resource
import com.hamric.domain.SearchUsersInfo
import com.hamric.domain.model.User
import com.hamric.domain.repository.UsersRepository
import com.hamric.domain.usecase.FetchUsersUseCase
import com.hamric.domain.usecase.ObserveCachedUsersUseCase
import com.hamric.domain.usecase.ObserveAndFilterCachedUsersUseCase
import com.hamric.domain.usecase.SearchUsersUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UsersViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    private lateinit var observeCachedUsers: ObserveCachedUsersUseCase
    private lateinit var fetchUsers: FetchUsersUseCase
    private lateinit var searchCachedUsers: ObserveAndFilterCachedUsersUseCase
    private lateinit var searchUsers: SearchUsersUseCase
    private lateinit var usersRepository: UsersRepository

    private val cachedList = MutableStateFlow<List<User>>(emptyList())
    private val cachedSearch = MutableStateFlow<List<User>>(emptyList())

    private lateinit var viewModel: UsersViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)

        observeCachedUsers = mockk()
        fetchUsers = mockk()
        searchCachedUsers = mockk()
        searchUsers = mockk()
        usersRepository = mockk()

        coEvery { usersRepository.lastListSince() } returns 0L

        every { observeCachedUsers() } returns cachedList
        every { searchCachedUsers(any()) } returns cachedSearch

        coEvery { fetchUsers(any()) } returns Resource.Success(0L)
        coEvery { searchUsers(any(), any(), any()) } returns
                Resource.Success(SearchUsersInfo(totalCount = 0, perPage = 20, currentPage = 1))
    }

    private fun buildViewModel(): UsersViewModel = UsersViewModel(
        observeCachedUsers = observeCachedUsers,
        fetchUsers = fetchUsers,
        searchCachedUsers = searchCachedUsers,
        searchUsers = searchUsers,
        usersRepository = usersRepository
    )

    @After
    fun tearDown() = Dispatchers.resetMain()


    @Test
    fun `initial state is LIST mode`() = runTest(dispatcher) {
        viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(UsersUiState.Mode.LIST, viewModel.state.value?.mode)
    }

    @Test
    fun `empty cache triggers initial fetch`() = runTest(dispatcher) {
        viewModel = buildViewModel()
        advanceUntilIdle()

        coVerify(atLeast = 1) { fetchUsers(0L) }
    }

    @Test
    fun `existing cache does not trigger initial fetch`() = runTest(dispatcher) {
        cachedList.value = listOf(User(1L, "a", "a", "a"))
        viewModel = buildViewModel()
        advanceUntilIdle()

        coVerify(exactly = 0) { fetchUsers(any()) }
    }

    @Test
    fun `list emits into state`() = runTest(dispatcher) {
        cachedList.value = listOf(User(1L, "octocat", "a", "h"))
        viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value!!
        assertEquals(1, state.users.size)
        assertEquals("octocat", state.users.first().login)
        assertEquals(UsersUiState.Mode.LIST, state.mode)
    }


    @Test
    fun `onListScrolledToEnd triggers next page fetch`() = runTest(dispatcher) {
        cachedList.value = listOf(User(1L, "a", "a", "a"))
        coEvery { usersRepository.lastListSince() } returns 30L
        coEvery { fetchUsers(30L) } returns Resource.Success(60L)

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onListScrolledToEnd()
        advanceUntilIdle()

        coVerify(atLeast = 1) { fetchUsers(30L) }
    }


    @Test
    fun `typing query switches to SEARCH mode`() = runTest(dispatcher) {
        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("octo")

        assertEquals(UsersUiState.Mode.SEARCH, viewModel.state.value?.mode)
        assertEquals("octo", viewModel.state.value?.query)
    }

    @Test
    fun `blank query does not trigger search`() = runTest(dispatcher) {
        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("")
        advanceUntilIdle()

        coVerify(exactly = 0) { searchUsers(any(), any(), any()) }
    }


    @Test
    fun `successful search clears isSearching, sets totalCount, shows cached results`() =
        runTest(dispatcher) {
            cachedSearch.value = listOf(User(1L, "octocat", "a", "h"))
            coEvery { searchUsers("octo", 1, any()) } returns
                    Resource.Success(
                        SearchUsersInfo(totalCount = 42, perPage = 20, currentPage = 1)
                    )

            viewModel = buildViewModel()
            advanceUntilIdle()

            viewModel.onQueryChange("octo")
            advanceTimeBy(1100)
            advanceUntilIdle()

            val state = viewModel.state.value!!
            assertEquals(UsersUiState.Mode.SEARCH, state.mode)
            assertFalse(state.isSearching)
            assertEquals(1, state.users.size)
            assertNull(state.error)
            assertEquals(42, viewModel.searchResultTotalCount.value)
        }

    @Test
    fun `search error sets error and clears isSearching`() = runTest(dispatcher) {
        coEvery { searchUsers(any(), any(), any()) } returns
                Resource.Error(RuntimeException("boom"))

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("octo")
        advanceTimeBy(1100)
        advanceUntilIdle()

        val state = viewModel.state.value!!
        assertEquals("boom", state.error)
        assertFalse(state.isSearching)
    }

    @Test
    fun `clearing query returns to LIST mode and resets totalCount`() = runTest(dispatcher) {
        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("octo")
        advanceTimeBy(1100)
        advanceUntilIdle()

        viewModel.onQueryChange("")
        advanceUntilIdle()

        assertEquals(UsersUiState.Mode.LIST, viewModel.state.value?.mode)
        assertNull(viewModel.searchResultTotalCount.value)
    }

    @Test
    fun `onSearchScrolledToEnd triggers next page`() = runTest(dispatcher) {
        coEvery { searchUsers("kotlin", 1, any()) } returns
                Resource.Success(SearchUsersInfo(totalCount = 100, perPage = 20, currentPage = 1))
        coEvery { searchUsers("kotlin", 2, any()) } returns
                Resource.Success(SearchUsersInfo(totalCount = 100, perPage = 20, currentPage = 2))

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("kotlin")
        advanceTimeBy(1100)
        advanceUntilIdle()

        viewModel.onSearchScrolledToEnd()
        advanceUntilIdle()

        coVerify(atLeast = 1) { searchUsers("kotlin", 2, any()) }
    }


    @Test
    fun `retry in SEARCH mode reruns search`() = runTest(dispatcher) {
        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onQueryChange("octo")
        advanceTimeBy(1100)
        advanceUntilIdle()

        viewModel.retry()
        advanceUntilIdle()

        coVerify(atLeast = 2) { searchUsers("octo", any(), any()) }
    }

    @Test
    fun `retry in LIST mode fetches next page`() = runTest(dispatcher) {
        cachedList.value = listOf(User(1L, "a", "a", "a"))
        coEvery { usersRepository.lastListSince() } returns 30L
        coEvery { fetchUsers(30L) } returns Resource.Success(60L)

        viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.retry()
        advanceUntilIdle()

        coVerify(atLeast = 1) { fetchUsers(30L) }
    }
}