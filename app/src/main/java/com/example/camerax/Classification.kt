package com.example.camerax


data class Classification(
    val score : List<Choice>,
    val modelo : String
)

data class Choice (
    val nome : String,
    val rating  : Float
)