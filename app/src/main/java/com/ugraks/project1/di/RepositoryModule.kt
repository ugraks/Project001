package com.ugraks.project1.di

import com.ugraks.project1.data.local.repository.ActivityRepository
import com.ugraks.project1.data.local.repository.ActivityRepositoryImpl
import com.ugraks.project1.data.local.repository.BoxingRepository
import com.ugraks.project1.data.local.repository.BoxingRepositoryImpl
import com.ugraks.project1.data.local.repository.CalorieRepositoryImpl
import com.ugraks.project1.data.local.repository.FitnessRepository
import com.ugraks.project1.data.local.repository.FitnessRepositoryImpl
import com.ugraks.project1.data.local.repository.FoodItemRepository
import com.ugraks.project1.data.local.repository.FoodItemRepositoryImpl
import com.ugraks.project1.data.local.repository.PedometerRepository
import com.ugraks.project1.data.local.repository.PedometerRepositoryImpl
import com.ugraks.project1.data.local.repository.RecipeRepository
import com.ugraks.project1.data.local.repository.RecipeRepositoryImpl
import com.ugraks.project1.data.repository.CalorieRepository
import dagger.Binds // Interface'i implementasyona bağlamak için
import dagger.Module // Hilt modülü olduğunu belirtir
import dagger.hilt.InstallIn // Modülün hangi Hilt bileşenine kurulacağını belirtir
import dagger.hilt.components.SingletonComponent // Uygulama yaşam döngüsü boyunca geçerli
import javax.inject.Singleton

// Repository bağımlılıklarını sağlamak için Hilt modülü
@Module // Bu bir Hilt modülüdür
@InstallIn(SingletonComponent::class) // Uygulama yaşam döngüsü boyunca geçerli olacak bağımlılıkları sağlar
abstract class RepositoryModule { // Binds kullanılıyorsa sınıf abstract olmalıdır

    // RecipeRepository arayüzünü RecipeRepositoryImpl somut sınıfına bağlar.
    // Hilt, bir yerde RecipeRepository istendiğinde, RecipeRepositoryImpl'in örneğini sağlayacağını bilir.
    // Hilt, RecipeRepositoryImpl'in bağımlılıklarını (RecipeDao, Context) otomatik olarak sağlar.
    @Binds // Bir arayüzü implementasyonuna bağlamak için kullanılır
    @Singleton
    abstract fun bindRecipeRepository( // Metot abstract olmalıdır
        recipeRepositoryImpl: RecipeRepositoryImpl // Bağlanacak somut sınıf
    ): RecipeRepository // Bağlanılan arayüz

    @Binds
    @Singleton
    abstract fun bindPedometerRepository( // Yeni metot
        pedometerRepositoryImpl: PedometerRepositoryImpl // Bağlanacak somut sınıf
    ): PedometerRepository // Bağlanılan arayüz

    @Binds // Yeni metot
    @Singleton
    abstract fun bindFoodItemRepository(
        foodItemRepositoryImpl: FoodItemRepositoryImpl // Bağlanacak somut sınıf
    ): FoodItemRepository // Bağlanılan arayüz

    @Binds // Bu metod bir arayüzü somut implementasyonuna bağlar
    @Singleton // Sağlanan bağımlılığın tek bir örneği olacak
    abstract fun bindCalorieRepository( // Metot abstract olmalı ve implementasyon sınıfını parametre almalı
        calorieRepositoryImpl: CalorieRepositoryImpl // Hilt bu implementasyonu sağlayabilir (@Inject constructor sayesinde)
    ): CalorieRepository // Metot Repository arayüzünü döndürmeli

    @Binds // <-- YENİ BAĞLAMA METODU
    @Singleton
    abstract fun bindFitnessRepository(
        fitnessRepositoryImpl: FitnessRepositoryImpl
    ): FitnessRepository // <-- FitnessRepository arayüzünü döndürür

    @Binds // <-- YENİ BAĞLAMA METODU
    @Singleton
    abstract fun bindBoxingRepository(
        boxingRepositoryImpl: BoxingRepositoryImpl
    ): BoxingRepository // <-- BoxingRepository arayüzünü döndürür

    @Binds // Bu fonksiyon, bir arayüzün hangi implementasyon ile sağlanacağını belirtir
    @Singleton // Sağlanan bağımlılığın Singleton olacağını belirtir
    abstract fun bindActivityRepository(
        activityRepositoryImpl: ActivityRepositoryImpl
    ): ActivityRepository
}