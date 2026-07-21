package com.felipe.topografiaapp.di

import android.content.Context
import com.felipe.topografiaapp.data.local.AppDatabase
import com.felipe.topografiaapp.RetrofitClient
import com.felipe.topografiaapp.data.local.dao.CanchaDao
import com.felipe.topografiaapp.data.local.dao.FundoDao
import com.felipe.topografiaapp.data.local.dao.PRDao
import com.felipe.topografiaapp.data.remote.ApiService
import com.felipe.topografiaapp.data.repository.TopoRepository
import com.felipe.topografiaapp.data.source.CoordConverter
import com.felipe.topografiaapp.domain.repository.ITopoRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getDatabase(context)

    @Provides
    fun provideFundoDao(db: AppDatabase): FundoDao = db.fundoDao()

    @Provides
    fun provideCanchaDao(db: AppDatabase): CanchaDao = db.canchaDao()

    @Provides
    fun providePRDao(db: AppDatabase): PRDao = db.prDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService =
        Retrofit.Builder()
            .baseUrl(RetrofitClient.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTopoRepository(impl: TopoRepository): ITopoRepository
}