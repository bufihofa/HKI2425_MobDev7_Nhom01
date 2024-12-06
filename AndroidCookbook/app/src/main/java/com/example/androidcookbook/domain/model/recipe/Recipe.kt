package com.example.androidcookbook.domain.model.recipe

data class RecipeList(
    val meals: List<Recipe>
)

data class Recipe(
    val idMeal: Int,
    val strMeal: String,
    val strCategory: String,
    val strArea: String,
    val strInstructions: String,
    val strMealThumb: String,

//    val strIngredient1: String,
//    val strIngredient2: String,
//    val strIngredient3: String,
//    val strIngredient4: String,
//    val strIngredient5: String,
//    val strIngredient6: String,
//    val strIngredient7: String,
//    val strIngredient8: String,
//    val strIngredient9: String,
//    val strIngredient10: String,
//    val strIngredient11: String,
//    val strIngredient12: String,
//    val strIngredient13: String,
//    val strIngredient14: String,
//    val strIngredient15: String,
//    val strIngredient16: String,
//    val strIngredient17: String,
//    val strIngredient18: String,
//    val strIngredient19: String,
//    val strIngredient20: String,
)

