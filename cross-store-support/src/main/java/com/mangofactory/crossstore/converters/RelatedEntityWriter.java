package com.mangofactory.crossstore.converters;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import com.mangofactory.crossstore.RelatedEntity;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class RelatedEntityWriter implements Converter<RelatedEntity, DBObject>  {

	@Autowired
	private EntityManagerFactory entityManagerFactory;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Override
	public DBObject convert(RelatedEntity source) {
		BasicDBObject result = new BasicDBObject();
		Object identifier = entityManagerFactory.getPersistenceUnitUtil().getIdentifier(source);
		result.put("_entityClass", source.getClass().getName());
		result.put("_entityId", identifier);
		
//		persistChanges(source);
		
		return result;
	}

	private void persistChanges(RelatedEntity source) {
		entityManager.persist(source);
	}



}
