Cross Store Support
===================

This library provided cross-store support similar to that provided by the [Spring Data Mongo Project](http://www.springsource.org/spring-data/mongodb).  However, aspect weaving is performed at runtime, meaning that there is no compile-time dependency on AspectJ.

Why this was created
--------------------

This project exists because I use Project Lombok extensively on an internal project.  However, Lombok is incompatible with AspectJ.  Therefore, this project exists to bridge the gap.

Should you use this project?
----------------------------

Probably not.  The Spring Data alternative is superior and better supported.  However, if - like me - you're blocked by the AspectJ dependency, then you may like to consider this implementation instead.

Limitations
-----------

 * When performing an update of a `@RelatedDocument`, the actual underlying document is removed from Mongo, and the new document is insterted.  (This makes an Update a Delete/Insert operation).

Features
--------

 * Provides support for `@RelatedDocument` in an `@Entity` class, which will be persisted to Mongo, instead of JPA.
 * Additionally, provides support for `RelatedEntity` in a Documents - ie., if a JPA entity exists in a Mongo document, a reference to the entity is persisted, rather than the entity itself.