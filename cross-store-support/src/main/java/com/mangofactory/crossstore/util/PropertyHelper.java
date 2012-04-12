package com.mangofactory.crossstore.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceUnitUtil;

import org.apache.commons.lang.NotImplementedException;

import com.mangofactory.crossstore.RelatedDocumentReference;
import com.mangofactory.crossstore.RelatedEntityReference;

public class PropertyHelper {

	private final Class<?> clazz;
	private List<Field> relatedDocumentFields = new ArrayList<Field>();
	private List<Field> relatedEntityFields = new ArrayList<Field>();
	
	public PropertyHelper(Class<?> clazz)
	{
		this.clazz = clazz;
	}
	public void addEntityField(Field field)
	{
		relatedEntityFields.add(field);
	}
	public void addEntityFields(List<Field> fields)
	{
		for (Field field : fields)
			relatedEntityFields.add(field);
	}
	public void addDocumentField(Field field) {
		relatedDocumentFields.add(field);
	}
	public void addDocumentFields(List<Field> list) {
		for (Field field : list)
			addDocumentField(field);
	}

	public void addDocumentMethods(List<Method> methods) {
		for (Method method : methods)
			addDocumentMethod(method);
	}
	public void addDocumentMethod(Method method) {
		throw new NotImplementedException();
	}
	public List<RelatedEntityReference> getEntityReferences(Object source, PersistenceUnitUtil persistenceUnitUtil)
	{
		List<RelatedEntityReference> references = new ArrayList<RelatedEntityReference>();
		for (Field field : relatedEntityFields)
		{
			 
		}
		return references;
	}
	public List<RelatedDocumentReference> getDocumentReferences(Object source, PersistenceUnitUtil persistenceUnitUtil) {
		List<RelatedDocumentReference> references = new ArrayList<RelatedDocumentReference>();
		for (Field field : relatedDocumentFields)
		{
			Object sourceId = persistenceUnitUtil.getIdentifier(source);
			RelatedDocumentReference documentReference = new RelatedDocumentReference(source, field,sourceId);
			if (documentReference.hasValue())
			{
				Object document = documentReference.getValue();
				Object documentId = ReflectionHelper.findDocumentIdValue(document);
				documentReference.setDocumentId(documentId);	
			}
			references.add(documentReference);
		}
		return references;
	}

	
}
