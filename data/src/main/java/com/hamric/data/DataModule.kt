package com.hamric.data

import com.hamric.data.repository.DetailUserRepositoryImpl
import com.hamric.data.repository.UsersRepositoryImpl
import com.hamric.domain.repository.DetailUserRepository
import com.hamric.domain.repository.UsersRepository
import org.koin.dsl.module

val dataModule = module{
    single<UsersRepository>{
        UsersRepositoryImpl(get(),get(),get(),get())
    }
    single<DetailUserRepository>{
        DetailUserRepositoryImpl(get(), get(), get())
    }
}