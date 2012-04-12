package com.mangofactory.crossstore.converters;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import com.mangofactory.crossstore.RelatedEntity;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class RelatedEntityWriter implements Converter<RelatedEntity, DBObject>  {

	@Autowired
	private EntityManagerFactory entityManagerFactory;
	
	@Override
	public DBObject convert(RelatedEntity source) {
		BasicDBObject result = new BasicDBObject();
		Object identifier = entityManagerFactory.getPersistenceUnitUtil().getIdentifier(source);
		result.put("_entityClass", source.getClass().getName());
		result.put("_entityId", identifier);
		return result;
	}



}
