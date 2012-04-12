package com.mangofactory.crossstore.test;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.data.mongodb.crossstore.RelatedDocument;

import com.mangofactory.crossstore.RelatedEntity;

@Entity
public class Author implements RelatedEntity {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	@Transient @RelatedDocument
	private Book book;
	
	private String name;
	
	public static Author withName(String name)
	{
		Author author = new Author();
		author.name = name;
		return author;
	}
	public static Author withNameAndBook(String name, String bookTitle)
	{
		Author author = new Author();
		author.name = name;
		Book book = new Book();
		book.setAuthor(author);
		book.setTitle(bookTitle);
		author.book = book;
		return author;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Book getBook() {
		return book;
	}
	public void setBook(Book book) {
		this.book = book;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
