package com.hamric.domain.usecase

import com.hamric.core.common.Resource
import com.hamric.domain.model.DetailUser
import com.hamric.domain.repository.DetailUserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FetchDetailUserUseCaseTest {

    private lateinit var repository: DetailUserRepository
    private lateinit var useCase: FetchDetailUserUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = FetchDetailUserUseCase(repository)
    }

    @Test
    fun `blank login returns Error without calling repository`() = runTest {
        val result = useCase("")

        assertTrue(result is Resource.Error)
        coVerify(exactly = 0) { repository.fetchDetailUser(any()) }
    }

    @Test
    fun `whitespace login returns Error without calling repository`() = runTest {
        val result = useCase("   ")

        assertTrue(result is Resource.Error)
        coVerify(exactly = 0) { repository.fetchDetailUser(any()) }
    }

    @Test
    fun `trims login before delegating`() = runTest {
        val detail = sampleDetail()
        coEvery { repository.fetchDetailUser("octocat") } returns Resource.Success(detail)

        val result = useCase("  octocat  ")

        assertTrue(result is Resource.Success)
        assertEquals(detail, (result as Resource.Success).data)
        coVerify(exactly = 1) { repository.fetchDetailUser("octocat") }
    }

    @Test
    fun `propagates repository Error`() = runTest {
        val boom = RuntimeException("net down")
        coEvery { repository.fetchDetailUser(any()) } returns Resource.Error(boom)

        val result = useCase("octocat")

        assertTrue(result is Resource.Error)
        assertEquals(boom, (result as Resource.Error).throwable)
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