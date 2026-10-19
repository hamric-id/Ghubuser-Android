# Ghubuser App 🚀
Developed using Android Studio Quail 1 2026.1.1 Patch 2:
- Jetpack Compose + LiveData + Glide + RecylcerView
- MVVM + Clean Architecture
- Multi Module
- Koin
- Unit Test
- Retrofit + Okhttp + Moshi + Chucker
- Coroutines
- Room


## Improvement & Additional Features:
- Combine Jetpack Compose with Kotlin XML component (RecyclerView)
- show total count of search result
- Dual Mode Users Screen: integrate Search Screen and User List Screen into single screen. If Search Bar is empty then it became User List Screen mode, otherwise it became Search Screen mode.
- pagination on recylerview: auto fetch next page of list and search result if last item visible on screen
- show error information when bad network happen and retry button
- caching list users every fetch from endpoint GET /users and GET /search/users
- offline mode first on User List Screen mode
- Search Screen mode automaticly switch to fully offline mode (use list user cache on room) if HTTP request failed (for improve UX purpose)

## Challanges:
- It feels strange to use RecyclerView in Jetpack Compose, some adjustments are needed to make it work

## Why Choose MVVM + Clean Architecture:
    MVVM + Clean was chosen because easier for testing and majority of the industry uses this approach when working on Kotlin XML and some projects that have recently been converted from Kotlin XML to Jetpack Compose
    
    
