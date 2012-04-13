package com.mangofactory.crossstore.aop;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;

import com.mangofactory.crossstore.CrossStoreReferenceFinder;
import com.mangofactory.crossstore.RelatedDocumentReference;
import com.mangofactory.crossstore.RelatedEntityReference;
import com.mongodb.BasicDBObject;

public abstract class AbstractDocumentAspect {
	@Autowired
	protected MongoTemplate mongoTemplate;
	
	@Autowired
	protected MongoOperations mongoOperations;

	@Autowired
	protected EntityManagerFactory entityManagerFactory;
	
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
}
