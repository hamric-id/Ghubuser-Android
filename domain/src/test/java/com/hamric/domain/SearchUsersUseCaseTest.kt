package com.hamric.domain.usecase

import com.hamric.core.common.Resource
import com.hamric.domain.SearchUsersInfo
import com.hamric.domain.repository.UsersRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchUsersUseCaseTest {

    private lateinit var repository: UsersRepository
    private lateinit var useCase: SearchUsersUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SearchUsersUseCase(repository)
    }

    @Test
    fun `blank query returns Success without calling repository`() = runTest {
        val result = useCase("")

        assertTrue(result is Resource.Success)
        coVerify(exactly = 0) { repository.searchUsers(any()) }
    }

    @Test
    fun `whitespace-only query returns Success without calling repository`() = runTest {
        val result = useCase("   ")

        assertTrue(result is Resource.Success)
        coVerify(exactly = 0) { repository.searchUsers(any()) }
    }

    @Test
    fun `forwards page argument`() = runTest {
        coEvery { repository.searchUsers("kotlin", 3) } returns Resource.Success(SearchUsersInfo(1,1,1))

        useCase("kotlin", page = 3)

        coVerify(exactly = 1) { repository.searchUsers("kotlin", 3) }
    }

    @Test
    fun `propagates repository Error`() = runTest {
        val boom = RuntimeException("network down")
        coEvery { repository.searchUsers(any()) } returns Resource.Error(boom)

        val result = useCase("kotlin")

        assertTrue(result is Resource.Error)
        assertEquals(boom, (result as Resource.Error).throwable)
    }
}