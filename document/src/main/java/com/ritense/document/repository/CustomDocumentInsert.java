package com.ritense.document.repository;

import com.ritense.document.domain.Document;

public interface CustomDocumentInsert {

    void insertForTenant(Document document, String tenantId);

}
