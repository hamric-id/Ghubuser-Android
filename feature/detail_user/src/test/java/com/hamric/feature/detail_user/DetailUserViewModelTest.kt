package com.hamric.feature.detail_user

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hamric.core.common.Resource
import com.hamric.domain.model.DetailUser
import com.hamric.domain.repository.DetailUserRepository
import com.hamric.domain.usecase.FetchDetailUserUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
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
class DetailUserViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    private lateinit var fetchDetailUser: FetchDetailUserUseCase
    private lateinit var repository: DetailUserRepository

    private val cachedFlow = MutableStateFlow<DetailUser?>(null)

    private lateinit var viewModel: DetailUserViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        fetchDetailUser = mockk()
        repository = mockk()

        every { repository.observeCachedDetailUser(any()) } returns cachedFlow
        coEvery { repository.touch(any()) } returns Unit
        coEvery { fetchDetailUser(any()) } returns Resource.Success(sampleDetail())
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun build() {
        viewModel = DetailUserViewModel(fetchDetailUser, repository)
    }

    @Test
    fun `load fetches from use case`() = runTest(dispatcher) {
        build()
        viewModel.load("octocat")
        advanceUntilIdle()

        coVerify(exactly = 1) { fetchDetailUser("octocat") }
    }

    @Test
    fun `load with blank login is a no-op`() = runTest(dispatcher) {
        build()
        viewModel.load("")
        advanceUntilIdle()

        coVerify(exactly = 0) { fetchDetailUser(any()) }
    }

    @Test
    fun `success populates user and clears isLoading`() = runTest(dispatcher) {
        build()
        viewModel.load("octocat")
        advanceUntilIdle()

        val state = viewModel.state.value!!
        assertEquals("octocat", state.user?.login)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `error sets error and clears isLoading`() = runTest(dispatcher) {
        coEvery { fetchDetailUser(any()) } returns Resource.Error(RuntimeException("boom"))
        build()
        viewModel.load("octocat")
        advanceUntilIdle()

        val state = viewModel.state.value!!
        assertEquals("boom", state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `cache emission populates user`() = runTest(dispatcher) {
        build()
        viewModel.load("octocat")
        cachedFlow.value = sampleDetail()
        advanceUntilIdle()

        val state = viewModel.state.value!!
        assertEquals("octocat", state.user?.login)
    }

    @Test
    fun `cache emission touches lastAccessedAt`() = runTest(dispatcher) {
        build()
        viewModel.load("octocat")
        cachedFlow.value = sampleDetail()
        advanceUntilIdle()

        coVerify(atLeast = 1) { repository.touch("octocat") }
    }

    @Test
    fun `retry refetches for current login`() = runTest(dispatcher) {
        build()
        viewModel.load("octocat")
        advanceUntilIdle()

        viewModel.retry()
        advanceUntilIdle()

        coVerify(atLeast = 2) { fetchDetailUser("octocat") }
    }

    @Test
    fun `duplicate load for same login does not refetch`() = runTest(dispatcher) {
        build()
        viewModel.load("octocat")
        advanceUntilIdle()

        viewModel.load("octocat")
        advanceUntilIdle()

        coVerify(exactly = 1) { fetchDetailUser("octocat") }
    }

    private fun sampleDetail() = DetailUser(
        login = "octocat",
        id = 1L,
        avatarUrl = "https://avatar/1",
        htmlUrl = "https://html/1",
        name = "The Octocat",
        company = "@github",
        blog = "https://github.blog",
        location = "SF",
        bio = "hi",
        twitterUsername = null,
        publicRepos = 8,
        publicGists = 8,
        followers = 100,
        following = 9,
        createdAt = "2011-01-25",
        updatedAt = "2021-01-01"
    )
}