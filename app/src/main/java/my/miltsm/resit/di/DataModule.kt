package my.miltsm.resit.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import my.miltsm.resit.data.DB

@InstallIn(SingletonComponent::class)
@Module
class DataModule {
    @Provides
    fun provideDB(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, DB::class.java, DB.NAME).build()
}