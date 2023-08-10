package com.ritense.document.repository;

import com.ritense.document.domain.Document;

public interface CustomDocumentUpdate {

    void updateByTenant(Document document, String tenantId);

}
