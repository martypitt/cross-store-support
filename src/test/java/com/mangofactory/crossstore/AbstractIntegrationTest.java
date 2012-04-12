package com.mangofactory.crossstore;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:AbstractIntegrationTest-context.xml")
public abstract class AbstractIntegrationTest  {

	@Autowired
	protected MongoOperations mongoOperations;
	
	@Before
	public void setup()
	{
	}
	
}
