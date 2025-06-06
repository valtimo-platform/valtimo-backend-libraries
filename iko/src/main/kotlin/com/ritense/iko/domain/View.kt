package com.ritense.iko.domain

class View(
    val name: String,
    val url: String,
) {
    companion object {
        fun create(name: String, url: String): View {
            return View(
                name = name,
                url = url
            )
        }
    }
}