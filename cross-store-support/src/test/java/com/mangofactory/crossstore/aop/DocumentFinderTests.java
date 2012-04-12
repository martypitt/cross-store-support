package com.mangofactory.crossstore.aop;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.mangofactory.crossstore.AbstractIntegrationTest;
import com.mangofactory.crossstore.CrossStoreReferenceFinder;
import com.mangofactory.crossstore.RelatedDocumentReference;
import com.mangofactory.crossstore.test.Author;

public class DocumentFinderTests extends AbstractIntegrationTest {

	@Autowired
	private EntityManagerFactory entityManagerFactory;
	

	private Author createdAuthor;
	@Before
	public void setup()
	{
		Author author = Author.withNameAndBook("Josh Bloch", "Effective Java");
		author.setId(1L);
		createdAuthor = author;
	}
	@Test
	public void findsFields()
	{
		CrossStoreReferenceFinder documentFinder = new CrossStoreReferenceFinder(createdAuthor, entityManagerFactory.getPersistenceUnitUtil());
		assertThat(documentFinder.getDocumentReferences().size(),equalTo(1));
		RelatedDocumentReference documentReference = documentFinder.getDocumentReferences().get(0);
	}
	@Test
	public void canReadValueOfPrivateField()
	{
		CrossStoreReferenceFinder documentFinder = new CrossStoreReferenceFinder(createdAuthor, entityManagerFactory.getPersistenceUnitUtil());
		RelatedDocumentReference documentReference = documentFinder.getDocumentReferences().get(0);
		Object value = documentReference.getValue();
		assertThat(value, notNullValue());
	}
}
