package com.mangofactory.crossstore.aop;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.mangofactory.crossstore.AbstractIntegrationTest;
import com.mangofactory.crossstore.dao.document.BookDAO;
import com.mangofactory.crossstore.dao.jpa.AuthorDAO;
import com.mangofactory.crossstore.test.Author;
import com.mangofactory.crossstore.test.Book;

@Transactional
public class DocumentLoadingAspectTests extends AbstractIntegrationTest {

	@Autowired
	private AuthorDAO authorDAO;
	
	@Autowired
	private BookDAO bookDAO;

	private Author createdAuthor;
	@Before
	@Override
	public void setup()
	{
		super.setup();
		mongoOperations.dropCollection(Book.class);
		Author author = Author.withNameAndBook("Josh Bloch", "Effective Java");
		authorDAO.save(author);
		createdAuthor = author;
	}
	@Test
	public void afterSavingThatBookIdIsPopulated()
	{
		Book book = createdAuthor.getBook();
		assertThat(book.getId(), notNullValue());
	}
			
	@Test
	public void afterSavingThatBookExistsInMongo()
	{
		assertThat(bookDAO.count(), equalTo(1L));
		String bookId = createdAuthor.getBook().getId();
		Book book = bookDAO.findOne(bookId);
		assertThat(book.getTitle(), equalTo("Effective Java"));
		assertThat(book.getAuthor().getName(),equalTo("Josh Bloch"));
	}
	
	@Test
	public void testLoadingManyFromRepo()
	{
		List<Author> list = authorDAO.findAll();
		Author author = list.get(0);
		assertThat(author.getBook(), notNullValue());
		assertThat(author.getBook().getTitle(), equalTo("Effective Java"));
		assertThat(author.getBook().getAuthor(), sameInstance(author));
	}
	@Test
	public void testLoadingFromRepo()
	{
		Author author = authorDAO.findOne(createdAuthor.getId());
		assertThat(author.getBook(), notNullValue());
		assertThat(author.getBook().getTitle(), equalTo("Effective Java"));
		assertThat(author.getBook().getAuthor(), sameInstance(author));
	}
	
	@Test
	public void afterUpdatingThatOnlySingleEntityExists()
	{
		createdAuthor.getBook().setTitle("Head first design patterns");
		authorDAO.save(createdAuthor);
		
		Author author = authorDAO.findOne(createdAuthor.getId());
		assertThat(author.getBook(), notNullValue());
		assertThat(author.getBook().getTitle(), equalTo("Head first design patterns"));
		assertThat(author.getBook().getAuthor(), sameInstance(author));
	}
	
	
	@Test
	public void testLoadingStandardBook()
	{
		Book book = new Book();
		book.setTitle("Test");
		bookDAO.save(book);
		
		Iterable<Book> all = bookDAO.findAll();
	}
}
