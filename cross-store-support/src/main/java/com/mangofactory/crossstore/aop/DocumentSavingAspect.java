package com.mangofactory.crossstore.aop;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.mangofactory.crossstore.RelatedDocumentReference;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


@Component
@Aspect
public class DocumentSavingAspect extends AbstractDocumentAspect {
	
	@Around("execution(* com.mangofactory.crossstore.repository.CrossStoreJpaRepository.save(..))")
	public Object saveDocumentsAfterSave(ProceedingJoinPoint pjp) throws Throwable
	{
		Object unsavedEntity = pjp.getArgs()[0];
		Object savedEntity = pjp.proceed();
		if (savedEntity == null)
		{
			return savedEntity;
		}
		List<RelatedDocumentReference> documents = findDocuments(unsavedEntity);
		for (RelatedDocumentReference relatedDocument : documents) {
			// TODO : For now we do a delete/insert.  In future, this should do an update.
			removeIfExists(relatedDocument);
			
			if (relatedDocument.hasValue())
			{
				DBObject dbObject = relatedDocument.getDBObject(mongoTemplate.getConverter());
				mongoTemplate.save(dbObject,getCollectionName(relatedDocument.getCollectionClass()));
			}
		}
		return savedEntity;
	}

	private void removeIfExists(RelatedDocumentReference relatedDocument) {
		BasicDBObject dbDocument = retrieveRelatedDocument(relatedDocument);
		if (dbDocument != null)
		{
			ObjectId objectId = dbDocument.getObjectId("_id");
			mongoTemplate.remove(query(where("_id").is(objectId)), getCollectionName(relatedDocument));
		}
	}

}
