package com.ritense.iko.service

import com.ritense.case_.domain.tab.CaseWidgetTab
import com.ritense.case_.widget.CaseWidgetDataProvider
import com.ritense.case_.widget.fields.FieldsCaseWidget
import org.springframework.data.domain.Pageable
import java.util.UUID

class FieldsIkoWidgetDataProvider(
    private val ikoValueResolverService: IkoValueResolverService
) : CaseWidgetDataProvider<FieldsCaseWidget> {

    override fun supportedWidgetType() = FieldsCaseWidget::class.java

    override fun getData(
        queryString: String,
        widgetTab: CaseWidgetTab,
        widget: FieldsCaseWidget,
        pageable: Pageable
    ): Any? {
        val valueKeyMap = widget.properties.columns.flatMap { column ->
            column.map { field ->
                field.value to field.key
            }
        }.toMap()

        val resolvedValues = ikoValueResolverService.resolveValues(queryString as QueryString, valueKeyMap.keys)

        return widget.properties.columns.flatMap { column ->
            column.map { field ->
                field.key to (resolvedValues[field.value] ?: null)
            }
        }.toMap()
    }

    override fun getData(
        documentId: UUID,
        widgetTab: CaseWidgetTab,
        widget: FieldsCaseWidget,
        pageable: Pageable
    ): Any {
        TODO("Not yet implemented")
    }

}