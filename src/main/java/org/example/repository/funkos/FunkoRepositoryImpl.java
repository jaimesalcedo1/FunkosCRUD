package org.example.repository.funkos;



import org.example.database.DataBaseManager;
import org.example.exceptions.FunkoException;
import org.example.exceptions.FunkoNoEncontradoException;
import org.example.models.Funkos;
import org.example.models.Modelo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FunkoRepositoryImpl implements FunkoRepository{

    private static FunkoRepositoryImpl instance;
    private final Logger logger = LoggerFactory.getLogger(FunkoRepositoryImpl.class);
    private final DataBaseManager dbm;

    public FunkoRepositoryImpl(DataBaseManager dbm) {
        this.dbm = dbm;
    }

    public static FunkoRepositoryImpl getInstance(DataBaseManager dbm){
        if(instance == null){
            instance = new FunkoRepositoryImpl(dbm);
        }
        return instance;
    }

    @Override
    public Funkos save(Funkos funko) throws SQLException, FunkoException {
        logger.debug("Guardando la medición: " + funko);
        String query = "INSERT INTO FUNKOS(cod, nombre, modelo, precio, fecha_lanzamiento) VALUES (?, ?, ?, ?, ?)";

        try (var connection = dbm.getConnection();
             var pstm = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            pstm.setString(1, funko.getCOD().toString());
            pstm.setString(2, funko.getNOMBRE());
            pstm.setString(3, funko.getMODELO().toString());
            pstm.setDouble(4, funko.getPRECIO());
            pstm.setString(5, String.valueOf(funko.getFECHA_LANZAMIENTO()));
            pstm.executeUpdate();
        }
        return funko;
    }

    @Override
    public Funkos update(Funkos funko) throws SQLException, FunkoException {
        logger.debug("Actualizando la medición: " + funko);
        String query = "UPDATE FUNKOS SET nombre =?, modelo =?, precio =?, fecha_lanzamiento =? WHERE cod =?";
        try (var connection = dbm.getConnection();
             var pstm = connection.prepareStatement(query)
        ) {
            pstm.setString(1, funko.getCOD().toString());
            pstm.setString(2, funko.getNOMBRE());
            pstm.setString(3, funko.getMODELO().toString());
            pstm.setDouble(4, funko.getPRECIO());
            pstm.setString(5, String.valueOf(funko.getFECHA_LANZAMIENTO()));

            var res = pstm.executeUpdate();
            if (res > 0) {
                logger.debug("funko actualizado");
            } else {
                logger.error("funko no actualizado al no encontrarse en la base de datos con codigo" + funko.getCOD());
                throw new FunkoNoEncontradoException("funko no encontrada");
            }
            return funko;
        }
    }

    @Override
    public Optional<Funkos> findById(Long id) throws SQLException {
        logger.debug("Obteniendo el funko con id: " + id);
        String query = "SELECT * FROM FUNKOS WHERE ID = ?";
        try (var connection = dbm.getConnection();
             var stmt = connection.prepareStatement(query)
        ) {
            stmt.setLong(1, id);
            var rs = stmt.executeQuery();
            Optional<Funkos> funko = Optional.empty();
            while (rs.next()) {

                funko = Optional.of(Funkos.builder()
                        .COD(UUID.fromString(rs.getString("cod")))
                        .NOMBRE(rs.getString("nombre"))
                        .MODELO(Modelo.valueOf(rs.getString("modelo")))
                        .PRECIO(rs.getDouble("precio"))
                        .FECHA_LANZAMIENTO(LocalDate.parse(rs.getString("fecha_lanzamiento")))
                        .build()

                );
            }
            return funko;
        }
    }

    @Override
    public List<Funkos> findAll() throws SQLException {
        logger.debug("Obteniendo todos los funkos");
        var query = "SELECT * FROM FUNKOS";
        try (var connection = dbm.getConnection();
             var pstm = connection.prepareStatement(query)
        ) {
            var rs = pstm.executeQuery();
            var lista = new ArrayList<Funkos>();
            while (rs.next()) {
                Funkos funko = Funkos.builder()
                        .COD(UUID.fromString(rs.getString("cod")))
                        .NOMBRE(rs.getString("nombre"))
                        .MODELO(Modelo.valueOf(rs.getString("modelo")))
                        .PRECIO(rs.getDouble("precio"))
                        .FECHA_LANZAMIENTO(LocalDate.parse(rs.getString("fecha_lanzamiento")))
                        .build();
                lista.add(funko);
            }
            return lista;
        }
    }

    @Override
    public boolean deleteById(Long id) throws SQLException {
        logger.debug("Borrando el funko con id: " + id);
        String query = "DELETE FROM FUNKOS WHERE ID =?";
        try (var connection = dbm.getConnection();
             var pstm = connection.prepareStatement(query)
        ) {
            pstm.setLong(1, id);
            var res = pstm.executeUpdate();
            return res > 0;
        }
    }

    @Override
    public void deleteAll() throws SQLException {
        logger.debug("Borrando todos los funkos");
        String query = "DELETE FROM FUNKOS";
        try (var connection = dbm.getConnection();
             var pstm = connection.prepareStatement(query)
        ) {
            pstm.executeUpdate();
        }
    }

    @Override
    public List<Funkos> findByNombre(String nombre) throws SQLException {
        return null;
    }
}
