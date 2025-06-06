package com.ritense.iko.service

import com.ritense.iko.domain.Search
import com.ritense.iko.domain.View
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

class ConfigurationService {

    fun createView(name: String, url: String): View {
        return View.create(name, url)
    }

    fun getSearches(pageable: Pageable): Page<Search> {
        return Page.empty<Search>()
    }

}