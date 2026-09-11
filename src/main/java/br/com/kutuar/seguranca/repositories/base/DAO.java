package br.com.kutuar.seguranca.repositories.base;

import br.com.kutuar.seguranca.models.Escola;

import java.util.List;
import java.util.Optional;

public interface DAO<T, ID> {

    Escola save(T entity);

    void update(T entity);

    Optional<T> findById(ID id);

    List<T> findAll();
}
