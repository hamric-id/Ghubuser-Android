package com.hamric.ghubuser.di

import com.hamric.feature.users.UsersViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        UsersViewModel(get(),get(),get(),get(),get())
    }
}