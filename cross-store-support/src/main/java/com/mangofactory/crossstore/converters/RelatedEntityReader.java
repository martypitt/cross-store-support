package com.mangofactory.crossstore.converters;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import com.mangofactory.crossstore.RelatedEntity;
import com.mangofactory.crossstore.aop.DocumentLoadingAspect;
import com.mangofactory.crossstore.aop.ThreadLocalEntityCache;
import com.mongodb.BasicDBObject;

public class RelatedEntityReader implements ConditionalGenericConverter {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private DocumentLoadingAspect documentLoader;

	public Object convert(BasicDBObject source) {
		String className = source.getString("_entityClass");
		Class<?> entityClass;
		try {
			entityClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		Object entityKey = source.get("_entityId");

		Object entity = fetchEntity(entityClass, entityKey);
		return entity;
	}

	private Object fetchEntity(Class<?> entityClass, Object entityKey) {
		Object entity = null;
		if (ThreadLocalEntityCache.contains(entityClass, entityKey))
		{
			entity = ThreadLocalEntityCache.get(entityClass, entityKey);
		} else {
			entity = entityManager.find(entityClass, entityKey);
			if (entity != null)
			{
				ThreadLocalEntityCache.put(entity, entityKey);
				// Populate the entity .. it may have other references
				documentLoader.setDocumentsAfterLoad(entity);
			}
		}
		return entity;
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		Set<ConvertiblePair> result = new HashSet<ConvertiblePair>();
		result.add(new ConvertiblePair(BasicDBObject.class, RelatedEntity.class));
		return result;
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType,
			TypeDescriptor targetType) {
		return convert((BasicDBObject) source);
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return true;
	}
}
