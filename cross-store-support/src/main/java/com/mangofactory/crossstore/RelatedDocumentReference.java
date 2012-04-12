package com.mangofactory.crossstore;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.lang.reflect.Field;

import org.h2.table.TableLinkConnection;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.ReflectionUtils;

import com.mangofactory.crossstore.aop.ThreadLocalEntityCache;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class RelatedDocumentReference {
	private static final String ENTITY_CLASS = "_entity_class";
	private static final String ENTITY_ID = "_entity_id";
	private static final String ENTITY_FIELD_NAME = "_entity_field_name";
	private static final String ENTITY_FIELD_CLASS = "_entity_field_class";
	
	private final Object target;
	private final Field field;
	private final Class<?> collectionClass;
	private final Object ownerId;
	private final String cacheKey;
	private Object documentId;
	public RelatedDocumentReference(Object target, Field field, Object ownerId)
	{
		this.collectionClass = field.getType();
		this.target = target;
		this.field = field;
		this.ownerId = ownerId;
		
		StringBuilder sb = new StringBuilder();
		sb.append(ENTITY_ID).append("=").append(ownerId.toString()).append("|")
			.append(ENTITY_CLASS).append("=").append(target.getClass().getName()).append("|")
			.append(ENTITY_FIELD_NAME).append("=").append(field.getName());
		cacheKey = sb.toString();
	}
	public String getCacheKey()
	{
		return cacheKey;
	}
	public Query getQuery()
	{
		Query query = query(
				where(ENTITY_ID).is(ownerId)
				.and(ENTITY_CLASS).is(target.getClass().getName())
				.and(ENTITY_FIELD_NAME).is(field.getName()));
		return query;
	}
	
	public DBObject getDBObject(MongoConverter converter)
	{
		final DBObject dbObject = new BasicDBObject();
		dbObject.put(ENTITY_ID, ownerId);
		dbObject.put(ENTITY_CLASS, target.getClass().getName());
		dbObject.put(ENTITY_FIELD_NAME, field.getName());
		converter.write(getValue(), dbObject);
		return dbObject;
	}

	public void setValue(Object value)
	{
		ReflectionUtils.makeAccessible(field);
		try {
			field.set(target, value);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	public void setValue(BasicDBObject dbObject, MongoConverter converter) {
		Object value = converter.read(getCollectionClass(), dbObject);
		ThreadLocalEntityCache.putIfNotExists(cacheKey,value);
		setValue(value);
	}

	public Object getValue()
	{
		ReflectionUtils.makeAccessible(field);
		try {
			return field.get(target);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	public boolean hasValue() {
		return getValue() != null;
	}

	/**
	 * Indicates if the document has already been persisted.
	 */
	public boolean isPersisted() {
		return documentId != null;
	}

	public Class<?> getCollectionClass() {
		return collectionClass;
	}

	public void setDocumentId(Object documentId) {
		this.documentId = documentId;
	}
}
