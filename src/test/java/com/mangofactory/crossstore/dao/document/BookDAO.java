package com.mangofactory.crossstore.dao.document;

import org.springframework.data.repository.CrudRepository;

import com.mangofactory.crossstore.test.Book;

public interface BookDAO extends CrudRepository<Book, String> {

}
