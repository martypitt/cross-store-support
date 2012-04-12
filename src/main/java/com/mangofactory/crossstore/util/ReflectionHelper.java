package com.mangofactory.crossstore.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;

public class ReflectionHelper {

	private static void appendFieldsWithAnnotation(Class<?> targetClass,Class<? extends Annotation> annotation,List<Field> result)
	{
		Field[] fields = targetClass.getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(annotation))
			{
				result.add(field);
			}
		}
		if (targetClass.getSuperclass() != Object.class)
		{
			appendFieldsWithAnnotation(targetClass.getSuperclass(), annotation, result);
		}
	}
	
	public static List<Field> getFieldsWithAnnotation(Class<?> targetClass,Class<? extends Annotation> annotation)
	{
		List<Field> result = new ArrayList<Field>();
		appendFieldsWithAnnotation(targetClass, annotation, result);
		return result;
		
	}
	public static List<Field> getFieldsWithAnnotation(Object target,Class<? extends Annotation> annotation)
	{
		return getFieldsWithAnnotation(target.getClass(), annotation);
	}
	private static void appendMethodsWithAnnotation(Class<?> targetClass,Class<? extends Annotation> annotation,List<Method> result)
	{
		Method[] methods = targetClass.getDeclaredMethods();
		for (Method method : methods)
		{
			if (method.isAnnotationPresent(annotation))
			{
				result.add(method);
			}
		}
		if (targetClass.getSuperclass() != Object.class)
		{
			appendMethodsWithAnnotation(targetClass.getSuperclass(), annotation, result);
		}
	}
	
	public static List<Method> getMethodsWithAnnotation(Class<?> targetClass,Class<? extends Annotation> annotation)
	{
		List<Method> result = new ArrayList<Method>();
		appendMethodsWithAnnotation(targetClass, annotation, result);
		return result;
		
	}
	public static List<Method> getMethodsWithAnnotation(Object target,Class<? extends Annotation> annotation)
	{
		return getMethodsWithAnnotation(target.getClass(), annotation);
	}
	
	public static Object findDocumentIdValue(Object document) {
		List<Field> fields = ReflectionHelper.getFieldsWithAnnotation(document, Id.class);
		if (fields.size() == 0)
		{
			throw new IllegalStateException("No @Id field found on " + document.getClass().getName());
		}
		if (fields.size() > 1)
		{
			throw new IllegalStateException("Multiple @Id fields found on " + document.getClass().getName());
		}
		Field field = fields.get(0);
		if (!field.isAccessible())
		{
			field.setAccessible(true);
		}
		try {
			return field.get(document);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
