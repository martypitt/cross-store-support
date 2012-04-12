package com.mangofactory.crossstore.aop;

import java.util.HashMap;

import com.mangofactory.crossstore.RelatedDocumentReference;

public abstract class ThreadLocalEntityCache {

	private static ThreadLocal<HashMap<Object, Object>> cache = new ThreadLocal<HashMap<Object,Object>>() {
		@Override
		protected java.util.HashMap<Object,Object> initialValue() {
			return new HashMap<Object, Object>();
		}
	};
	
	public static boolean contains(Object entity, Object id)
	{
		return contains(entity.getClass(),id);
	}
	public static boolean contains(Class<?> entityClass, Object id)
	{
		EntityCacheKey key = new EntityCacheKey(entityClass, id);
		return cache.get().containsKey(key);
	}
	public static boolean contains(String cacheKey)
	{
		return cache.get().containsKey(cacheKey);
	}
	public static void reset()
	{
		cache.remove();
	}
	public static void putWithCacheKey(Object entity, String cacheKey)
	{
		cache.get().put(cacheKey, entity);
	}
	public static void put(Object entity, Object id)
	{
		EntityCacheKey key = new EntityCacheKey(entity.getClass(), id);
		HashMap<Object,Object> map = cache.get();
		map.put(key, entity);
	}
	public static Object get(Class<?> entityClass, Object id)
	{
		EntityCacheKey key = new EntityCacheKey(entityClass, id);
		return cache.get().get(key);
	}
	public static Object get(String cacheKey)
	{
		return cache.get().get(cacheKey);
	}
	
	static final class EntityCacheKey
	{
		private final Class<?> entityClass;
		private final Object id;

		public EntityCacheKey(Class<?> entityClass, Object id)
		{
			this.entityClass = entityClass;
			this.id = id;
		}

		public Class<?> getEntityClass() {
			return entityClass;
		}

		public Object getId() {
			return id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((entityClass == null) ? 0 : entityClass.hashCode());
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EntityCacheKey other = (EntityCacheKey) obj;
			if (entityClass == null) {
				if (other.entityClass != null)
					return false;
			} else if (!entityClass.equals(other.entityClass))
				return false;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}
	}

	public static void putIfNotExists(String cacheKey, Object value) {
		if (!contains(cacheKey))
			putWithCacheKey(value, cacheKey);
	}
}
