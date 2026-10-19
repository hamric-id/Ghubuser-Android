package com.hamric.domain.usecase

import app.cash.turbine.test
import com.hamric.domain.model.User
import com.hamric.domain.repository.UsersRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveAndFilterCachedUsersUseCaseTest {

    private lateinit var repository: UsersRepository
    private lateinit var useCase: ObserveAndFilterCachedUsersUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = ObserveAndFilterCachedUsersUseCase(repository)
    }

    @Test
    fun `forwards query to repository`() = runTest {
        every { repository.observeAndFilterCachedUsers("octo") } returns flowOf(emptyList())

        useCase("octo").test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) { repository.observeAndFilterCachedUsers("octo") }
    }

    @Test
    fun `emits what repository emits`() = runTest {
        val users = listOf(
            User(1L, "octocat", "https://avatar/1", "https://html/1"),
            User(2L, "torvalds", "https://avatar/2", "https://html/2")
        )
        every { repository.observeAndFilterCachedUsers(any()) } returns flowOf(users)

        useCase("x").test {
            assertEquals(users, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}