package com.mangofactory.crossstore.aop;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.BeanWrapper;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.MongoPersistentProperty;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.stereotype.Component;

import com.mangofactory.crossstore.RelatedDocumentReference;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Handles populating the ID returned from the db on a related document.
 * Related documents are enhaced with special fields, so we don't go through the
 * standard MongoTemplate for mapping and saving (at least, not directly).
 * 
 * Therefore, ID's are not natively mapped back to the underlying object.
 * 
 * This class listens for a specific entity to be updated, and populates the ID on the object.
 * 
 * @author martypitt
 *
 */
@Component
public class RelatedDocumentIdUpdater implements ApplicationListener<AfterSaveEvent<BasicDBObject>>{

	private static final String ID = "_id";
	private MongoConverter mongoConverter;
	private ThreadLocal<Map<DBObject,RelatedDocumentReference>> persisteningEntities = new ThreadLocal<Map<DBObject,RelatedDocumentReference>>(){
		@Override
		protected java.util.Map<DBObject,RelatedDocumentReference> initialValue() {
			return new HashMap<DBObject, RelatedDocumentReference>();
		}
	};
	private MappingContext<? extends MongoPersistentEntity<?>, MongoPersistentProperty> mappingContext;

	@Override
	public void onApplicationEvent(AfterSaveEvent<BasicDBObject> event) {
		Map<DBObject, RelatedDocumentReference> map = persisteningEntities.get();
		if (map.containsKey(event.getSource()))
		{
			RelatedDocumentReference relatedDocumentReference = map.get(event.getSource());
			populateIdIfNecessary(event, relatedDocumentReference);
		}
	}

	private MongoPersistentProperty getIdPropertyFor(Class<?> type) {
		return mappingContext.getPersistentEntity(type).getIdProperty();
	}
	public void listenFor(RelatedDocumentReference relatedDocument,
			DBObject dbObject) {
		persisteningEntities.get().put(dbObject, relatedDocument);
		
	}public void stopListeningFor(DBObject dbObject) {
		persisteningEntities.get().remove(dbObject);
	}
	/**
	 * Populates the id property of the saved object, if it's not set already.
	 * 
	 * @param savedObject
	 * @param id
	 */
	protected void populateIdIfNecessary(AfterSaveEvent<BasicDBObject> event, RelatedDocumentReference relatedDocument) {
		Object id = event.getDBObject().get(ID);
		if (id == null) {
			return;
		}

		MongoPersistentProperty idProp = getIdPropertyFor(relatedDocument.getCollectionClass());

		if (idProp == null) {
			return;
		}
		
		try {
			BeanWrapper.create(relatedDocument.getValue(), mongoConverter.getConversionService()).setProperty(idProp, id);
			return;
		} catch (IllegalAccessException e) {
			throw new MappingException(e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new MappingException(e.getMessage(), e);
		}
		
	}

	public MongoConverter getMongoConverter() {
		return mongoConverter;
	}

	public void setMongoConverter(MongoConverter mongoConverter) {
		this.mongoConverter = mongoConverter;
		this.mappingContext = mongoConverter.getMappingContext();
	}
}
