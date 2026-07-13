package com.example.model

data class Zikr(
    val id: Int,
    val text: String,
    val count: Int,
    val instructions: String? = null
)

data class Category(
    val id: Int,
    val title: String,
    val azkar: List<Zikr>
)
