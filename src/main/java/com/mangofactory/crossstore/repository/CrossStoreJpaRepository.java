package com.mangofactory.crossstore.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Marker interface to indicate the cross-store support
 * aspect should be weaved into the targeted repostiory
 * @author martypitt
 */
public interface CrossStoreJpaRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

}
