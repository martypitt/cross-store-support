package com.mangofactory.crossstore;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.PersistenceUnitUtil;

import org.springframework.data.mongodb.crossstore.RelatedDocument;

import com.mangofactory.crossstore.util.PropertyHelper;
import com.mangofactory.crossstore.util.ReflectionHelper;

public class CrossStoreReferenceFinder {

	private final Object source;
	private final PropertyHelper reflectionRegistry;
	private List<RelatedDocumentReference> documentReferences;
	private List<RelatedEntityReference> entityReferences;
	public CrossStoreReferenceFinder(Object source, PersistenceUnitUtil persistenceUnitUtil) {
		this.source = source;
		reflectionRegistry = buildReflectionCache();
		documentReferences = reflectionRegistry.getDocumentReferences(source,persistenceUnitUtil);
		entityReferences = reflectionRegistry.getEntityReferences(source, persistenceUnitUtil);
	}

	private PropertyHelper buildReflectionCache() {
		PropertyHelper registry = new PropertyHelper(source.getClass()); 
		addRelatedDocumentFields(registry);
//		addRelatedEntityFields(registry);
		// TODO : Methods are not implemented yet
//		addRelatedDocumentMethods(registry);
//		addRelatedEntityMethods(registry);
		return registry;
	}
/*
	private void addRelatedEntityMethods(PropertyHelper registry) {
		addRelatedEntityMethods(registry,source.getClass());
	}
	private void addRelatedEntityMethods(PropertyHelper registry,Class<?> targetClass) {
		List<Method> methods = ReflectionHelper.getMethodsWithAnnotation(source, RelatedEntity.class);
		// TODO - not implemented
	}

	private void addRelatedEntityFields(PropertyHelper registry) {
		addRelatedEntityFields(registry,source.getClass());
	}
	private void addRelatedEntityFields(PropertyHelper registry,Class<?> targetClass) {
		List<Field> list = ReflectionHelper.getFieldsWithAnnotation(targetClass, RelatedEntity.class);
		registry.addEntityFields(list);
	}
*/
	void addRelatedDocumentMethods(PropertyHelper registry) {
		List<Method> methods = ReflectionHelper.getMethodsWithAnnotation(source, RelatedDocument.class);
		registry.addDocumentMethods(methods);
	}

	
	void addRelatedDocumentFields(PropertyHelper registry) {
		addRelatedDocumentFields(registry,source.getClass());
	}
	void addRelatedDocumentFields(PropertyHelper registry,Class<?> targetClass) {
		List<Field> list = ReflectionHelper.getFieldsWithAnnotation(targetClass, RelatedDocument.class);
		registry.addDocumentFields(list);
	}
	public List<RelatedDocumentReference> getDocumentReferences() {
		return documentReferences;
	}
	public List<RelatedEntityReference> getEntityReferences()
	{
		return entityReferences;
	}

}
