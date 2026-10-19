package com.hamric.ghubuser.di

import com.hamric.core.common.DefaultDispatchersProvider
import com.hamric.core.common.DispatchersProvider
import com.hamric.domain.usecase.FetchDetailUserUseCase
import com.hamric.domain.usecase.FetchUsersUseCase
import com.hamric.domain.usecase.ObserveCachedUsersUseCase
import com.hamric.domain.usecase.ObserveAndFilterCachedUsersUseCase
import com.hamric.domain.usecase.SearchUsersUseCase
import org.koin.dsl.module

val appModule = module{
    single<DispatchersProvider> { DefaultDispatchersProvider() }

    factory { ObserveCachedUsersUseCase(get()) }
    factory { FetchUsersUseCase(get()) }
    factory { ObserveAndFilterCachedUsersUseCase(get()) }
    factory { SearchUsersUseCase(get()) }
    factory { FetchDetailUserUseCase(get()) }
}