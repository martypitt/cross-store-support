package com.mangofactory.crossstore.aop;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import com.mangofactory.crossstore.RelatedDocumentReference;
import com.mangofactory.crossstore.converters.PopulateDocumentsEvent;
import com.mongodb.BasicDBObject;

@Aspect
//@Component
public class DocumentLoadingAspect extends AbstractDocumentAspect {
	
	@Around("execution(* com.mangofactory.crossstore.repository.CrossStore*.find*(..))")
	public Object loadCrossStoreEntity(ProceedingJoinPoint pjp) throws Throwable
	{
		ThreadLocalEntityCache.reset();
		Object fetchedEntity = pjp.proceed();
		setDocumentsAfterLoad(fetchedEntity);
		return fetchedEntity;
	}

	public void setDocumentsAfterLoad(Object fetchedEntity) {
		if (fetchedEntity instanceof Iterable)
		{
			setCollectionAfterLoad((Iterable<?>) fetchedEntity);
			return;
		}

		cache(fetchedEntity);

		List<RelatedDocumentReference> documents = findDocuments(fetchedEntity);
		for (RelatedDocumentReference documentReference : documents) {
			populate(documentReference);
		}
	}

	void populate(RelatedDocumentReference documentReference) {
		if (ThreadLocalEntityCache.contains(documentReference.getCacheKey()))
		{
			documentReference.setValue(ThreadLocalEntityCache.get(documentReference.getCacheKey()));
		} else {
			BasicDBObject dbObject = retrieveRelatedDocument(documentReference);
			if (dbObject != null)
				documentReference.setValue(dbObject,mongoTemplate.getConverter());
		}
	}

	private void cache(Object fetchedEntity) {
		Object identifier = entityManagerFactory.getPersistenceUnitUtil().getIdentifier(fetchedEntity);
		ThreadLocalEntityCache.put(fetchedEntity, identifier);
	}

	private void setCollectionAfterLoad(Iterable<?> fetchedEntity) {
		for (Object object : fetchedEntity) {
			setDocumentsAfterLoad(object);
		}
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		super.onApplicationEvent(event);
		if (event instanceof PopulateDocumentsEvent)
		{
			setDocumentsAfterLoad(((PopulateDocumentsEvent)event).getEntity());	
		}
	}

}
