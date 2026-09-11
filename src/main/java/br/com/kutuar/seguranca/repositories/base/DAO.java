package br.com.kutuar.seguranca.repositories.base;

import java.util.List;
import java.util.Optional;

public interface DAO<T, ID> {

    T save(T entity);

    void update(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();
}
