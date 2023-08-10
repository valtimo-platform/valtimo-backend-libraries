package com.ritense.document.repository;

import com.ritense.document.domain.Document;
import org.springframework.data.repository.NoRepositoryBean;

public interface CustomDocumentInsert {

    void insertForTenant(Document document, String tenantId);

}
