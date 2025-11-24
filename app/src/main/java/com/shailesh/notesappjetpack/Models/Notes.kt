package com.shailesh.notesappjetpack.Models

data class Notes(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val pinned: Boolean = false // new field
)
