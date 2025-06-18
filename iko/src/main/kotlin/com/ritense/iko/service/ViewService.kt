package com.ritense.iko.service

import com.ritense.iko.domain.Search
import com.ritense.iko.domain.View
import com.ritense.iko.repository.ViewRepository
import com.ritense.iko.web.rest.request.CreateViewRequest
import com.ritense.iko.web.rest.request.UpdateViewRequest

class ViewService(
    private val viewRepository: ViewRepository
) {

    fun createView(request: CreateViewRequest): View {
        // First create the View instance
        val view = View.create(name = request.name)

        // Then add Searches and assign 'view' to each Search
        val searches = request.searches.map {
            Search(view = view, url = it.url)
        }

        // Add searches to the View
        view.searches.addAll(searches)

        // Save and return
        return viewRepository.save(view)
    }

    fun updateView(request: UpdateViewRequest) : View {
        // Find the View instance
        val view = viewRepository.getReferenceById(request.id)

        view.name = request.name

        val searches = request.searches.map {
            Search(view = view, url = it.url)
        }

        // Add searches to the View
        view.searches.addAll(searches)

        // Save and return
        return viewRepository.save(view)
    }

    fun deleteView() {

    }

}