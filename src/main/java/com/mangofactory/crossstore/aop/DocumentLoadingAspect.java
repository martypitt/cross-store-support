package com.mangofactory.crossstore.aop;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.h2.table.TableLinkConnection;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.mangofactory.crossstore.RelatedDocumentReference;
import com.mangofactory.crossstore.converters.PopulateDocumentsEvent;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Aspect
@Component
public class DocumentLoadingAspect extends AbstractDocumentAspect implements ApplicationListener<PopulateDocumentsEvent> {

	@Around("execution(* com.mangofactory.crossstore.repository.CrossStoreJpaRepository.find*(..))")
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
	public void onApplicationEvent(PopulateDocumentsEvent event) {
		setDocumentsAfterLoad(event.getEntity());
	}

}
