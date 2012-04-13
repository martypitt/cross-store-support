package com.mangofactory.crossstore.aop;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bson.types.ObjectId;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;

import com.mangofactory.crossstore.RelatedDocumentReference;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;


@Component
@Aspect
public class DocumentSavingAspect extends AbstractDocumentAspect  implements InitializingBean {
	
	@Autowired
	RelatedDocumentIdUpdater idUpdater;
	
	
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
				
				idUpdater.listenFor(relatedDocument,dbObject);
				mongoTemplate.save(dbObject,getCollectionName(relatedDocument.getCollectionClass()));
				idUpdater.stopListeningFor(dbObject);
			}
		}
		return savedEntity;
	}
	protected MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> getMappingContext()
	{
		return mongoTemplate.getConverter().getMappingContext();
	}
	private void removeIfExists(RelatedDocumentReference relatedDocument) {
		BasicDBObject dbDocument = retrieveRelatedDocument(relatedDocument);
		if (dbDocument != null)
		{
			ObjectId objectId = dbDocument.getObjectId("_id");
			mongoTemplate.remove(query(where("_id").is(objectId)), getCollectionName(relatedDocument));
		}
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		idUpdater.setMongoConverter(mongoTemplate.getConverter());
	}
}
