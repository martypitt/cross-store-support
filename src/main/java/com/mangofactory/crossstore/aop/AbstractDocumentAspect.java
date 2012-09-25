package com.mangofactory.crossstore.aop;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;

import com.mangofactory.crossstore.CrossStoreReferenceFinder;
import com.mangofactory.crossstore.RelatedDocumentReference;
import com.mangofactory.crossstore.RelatedEntityReference;
import com.mongodb.BasicDBObject;

public class AbstractDocumentAspect implements ApplicationContextAware, ApplicationListener<ApplicationEvent> {
	// Wired in once the context is initialized
	protected MongoTemplate mongoTemplate;
	protected MongoOperations mongoOperations;

	@Autowired
	protected EntityManagerFactory entityManagerFactory;

	private ApplicationContext applicationContext;

	private boolean initialized;
	
	protected List<RelatedEntityReference> findEntities(Object fetchedEntity)
	{
		if (fetchedEntity == null)
		{
			return Collections.emptyList();
		}
		CrossStoreReferenceFinder finder = new CrossStoreReferenceFinder(fetchedEntity,entityManagerFactory.getPersistenceUnitUtil());
		return finder.getEntityReferences();
	}
	protected List<RelatedDocumentReference> findDocuments(Object fetchedEntity)
	{
		if (fetchedEntity == null)
		{
			return Collections.emptyList();
		}
		CrossStoreReferenceFinder finder = new CrossStoreReferenceFinder(fetchedEntity,entityManagerFactory.getPersistenceUnitUtil());
		List<RelatedDocumentReference> documentReferences = finder.getDocumentReferences();
		return documentReferences;
	}
	protected String getCollectionName(Class<?> collectionClass) {
		return mongoOperations.getCollectionName(collectionClass);
	}
	protected String getCollectionName(RelatedDocumentReference documentReference)
	{
		return getCollectionName(documentReference.getCollectionClass());
	}
	protected BasicDBObject retrieveRelatedDocument(RelatedDocumentReference relatedDocumentReference) {
		List<BasicDBObject> results = mongoTemplate.find(relatedDocumentReference.getQuery(), BasicDBObject.class, getCollectionName(relatedDocumentReference.getCollectionClass()));
		if (results.size() > 1)
		{
			// TODO : Make this clearer
			throw new IllegalStateException("Received more than 1 match for related document");
		}
		BasicDBObject dbObject = null;
		if (results.size() == 1)
		{
			dbObject = results.get(0);
		}
		return dbObject;
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		
	}
	private void onSpringContainerInitialized(ContextRefreshedEvent event) {
		if (!initialized)
			initialize();
	}
	private void initialize() {
		// Fetch mongo-related beans explicitly from the container,
		// as declaring them as dependencies creates cyclical dependnecies
		this.mongoOperations = applicationContext.getBean(MongoOperations.class);
		this.mongoTemplate = applicationContext.getBean(MongoTemplate.class);
		afterInitialized();
		initialized = true;
	}
	protected void afterInitialized() {
	}
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ContextRefreshedEvent)
		{
			onSpringContainerInitialized((ContextRefreshedEvent) event);
		}
	}
	public boolean isInitialized() {
		return initialized;
	}
}
