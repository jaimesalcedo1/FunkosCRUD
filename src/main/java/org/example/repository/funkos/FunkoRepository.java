package org.example.repository.funkos;


import org.example.exceptions.FunkoException;
import org.example.models.Funkos;
import org.example.repository.crud.CrudRepository;

import java.sql.SQLException;
import java.util.List;

public interface FunkoRepository extends CrudRepository<Funkos, Long, FunkoException> {
    List<Funkos> findByNombre(String nombre) throws SQLException;
}
