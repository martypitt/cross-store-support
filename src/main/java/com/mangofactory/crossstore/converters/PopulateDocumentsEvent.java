package com.mangofactory.crossstore.converters;

import org.springframework.context.ApplicationEvent;

public class PopulateDocumentsEvent extends ApplicationEvent {

	public PopulateDocumentsEvent(Object entity) {
		super(entity);
	}
	public Object getEntity()
	{
		return getSource();
	}

}
