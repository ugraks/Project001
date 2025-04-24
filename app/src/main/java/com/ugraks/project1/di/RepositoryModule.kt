package com.ugraks.project1.di

import com.ugraks.project1.data.local.repository.FoodItemRepository
import com.ugraks.project1.data.local.repository.FoodItemRepositoryImpl
import com.ugraks.project1.data.local.repository.PedometerRepository
import com.ugraks.project1.data.local.repository.PedometerRepositoryImpl
import com.ugraks.project1.data.local.repository.RecipeRepository
import com.ugraks.project1.data.local.repository.RecipeRepositoryImpl
import dagger.Binds // Interface'i implementasyona bağlamak için
import dagger.Module // Hilt modülü olduğunu belirtir
import dagger.hilt.InstallIn // Modülün hangi Hilt bileşenine kurulacağını belirtir
import dagger.hilt.components.SingletonComponent // Uygulama yaşam döngüsü boyunca geçerli

// Repository bağımlılıklarını sağlamak için Hilt modülü
@Module // Bu bir Hilt modülüdür
@InstallIn(SingletonComponent::class) // Uygulama yaşam döngüsü boyunca geçerli olacak bağımlılıkları sağlar
abstract class RepositoryModule { // Binds kullanılıyorsa sınıf abstract olmalıdır

    // RecipeRepository arayüzünü RecipeRepositoryImpl somut sınıfına bağlar.
    // Hilt, bir yerde RecipeRepository istendiğinde, RecipeRepositoryImpl'in örneğini sağlayacağını bilir.
    // Hilt, RecipeRepositoryImpl'in bağımlılıklarını (RecipeDao, Context) otomatik olarak sağlar.
    @Binds // Bir arayüzü implementasyonuna bağlamak için kullanılır
    abstract fun bindRecipeRepository( // Metot abstract olmalıdır
        recipeRepositoryImpl: RecipeRepositoryImpl // Bağlanacak somut sınıf
    ): RecipeRepository // Bağlanılan arayüz

    @Binds
    abstract fun bindPedometerRepository( // Yeni metot
        pedometerRepositoryImpl: PedometerRepositoryImpl // Bağlanacak somut sınıf
    ): PedometerRepository // Bağlanılan arayüz

    @Binds // Yeni metot
    abstract fun bindFoodItemRepository(
        foodItemRepositoryImpl: FoodItemRepositoryImpl // Bağlanacak somut sınıf
    ): FoodItemRepository // Bağlanılan arayüz
}