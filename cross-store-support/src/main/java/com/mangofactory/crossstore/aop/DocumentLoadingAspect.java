package com.mangofactory.crossstore.aop;

import java.util.List;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.mangofactory.crossstore.RelatedDocumentReference;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Aspect
@Component
public class DocumentLoadingAspect extends AbstractDocumentAspect {

	@AfterReturning(
			pointcut="execution(* com.mangofactory.crossstore.repository.CrossStoreJpaRepository.find*(..))",
			returning="fetchedEntity"
			)
	public void setDocumentsAfterLoad(Object fetchedEntity)
	{
		if (fetchedEntity instanceof Iterable)
		{
			setCollectionAfterLoad((Iterable<?>) fetchedEntity);
			return;
		}
		List<RelatedDocumentReference> documents = findDocuments(fetchedEntity);
		for (RelatedDocumentReference relatedDocumentReference : documents) {
			BasicDBObject dbObject = retrieveRelatedDocument(relatedDocumentReference);
			if (dbObject != null)
				relatedDocumentReference.setValue(dbObject,mongoTemplate.getConverter());

		}
	}

	private void setCollectionAfterLoad(Iterable<?> fetchedEntity) {
		for (Object object : fetchedEntity) {
			setDocumentsAfterLoad(object);
		}
	}

}
