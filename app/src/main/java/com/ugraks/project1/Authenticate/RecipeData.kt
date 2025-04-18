package com.ugraks.project1.Authenticate


import android.content.Context


fun readRecipesFromAssets(context: Context): List<Recipe> {
    val recipeList = mutableListOf<Recipe>()
    val inputStream = context.assets.open("recipes.txt")
    val lines = inputStream.bufferedReader().readLines()

    var currentName: String? = null
    val currentIngredients = mutableListOf<String>()
    var currentInstructions = StringBuilder()
    var readingIngredients = false
    var readingInstructions = false

    fun saveCurrentRecipe() {
        if (currentName != null) {
            recipeList.add(
                Recipe(
                    name = currentName!!,
                    ingredients = currentIngredients.toList(),
                    instructions = currentInstructions.toString().trim()
                )
            )
        }
        currentName = null
        currentIngredients.clear()
        currentInstructions = StringBuilder()
        readingIngredients = false
        readingInstructions = false
    }

    for (line in lines) {
        when {
            line.startsWith("[") && line.endsWith("]") -> {
                saveCurrentRecipe()
                currentName = line.removeSurrounding("[", "]")
            }

            line.startsWith("Ingredients:", true) -> {
                readingIngredients = true
                readingInstructions = false
            }

            line.startsWith("Instructions:", true) -> {
                readingIngredients = false
                readingInstructions = true
            }

            line.isBlank() -> continue

            readingIngredients -> currentIngredients.add(line.trimStart('-').trim())
            readingInstructions -> currentInstructions.appendLine(line.trim())
        }
    }

    saveCurrentRecipe() // Son tarifi ekle

    return recipeList
}

data class Recipe(
    val name: String,
    val ingredients: List<String>,
    val instructions: String
)


