package org.example.database;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.example.models.Funkos;
import org.example.models.Modelo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Handler;

public class DataBaseManager implements AutoCloseable{

    private static DataBaseManager instance;
    private final Logger logger = LoggerFactory.getLogger(DataBaseManager.class);
    private boolean databaseInitTables = false;
    private String serverPort;
    private String databaseName;
    private String jdbcDriver;
    private String connectionUrl;
    private String databaseUser;
    private String databasePassword;
    private String databaseInitScript = "init.sql";
    private Connection connection;

    private DataBaseManager(){
        loadProperties();

        try{
            openConnection();
            if(databaseInitTables){
                initTables();
            }
            closeConnection();
        } catch (SQLException e){
            logger.error("Error al conectar con la base de datos" + e.getMessage());
        }
    }

    public static synchronized DataBaseManager getInstance(){
        if(instance == null){
            instance = new DataBaseManager();
        }
        return instance;
    }

    public void loadProperties(){
        logger.debug("Cargando fichero de la configuración para la base de datos");
        try {
            var archivo = ClassLoader.getSystemResource("config.properties").getFile();
            var properties = new Properties();
            properties.load(new FileReader(archivo));

            serverPort = properties.getProperty("database.port", "3306");
            databaseName = properties.getProperty("database.name", "AppDataBase");
            jdbcDriver = properties.getProperty("database.driver", "org.h2.Driver");
            databaseInitTables = Boolean.parseBoolean(properties.getProperty("database.initTables", "false"));
            databaseInitScript = properties.getProperty("database.initScript", "init.sql");
            databaseUser = properties.getProperty("database.user", "sa");
            databasePassword = properties.getProperty("database.password", "");
            connectionUrl =
                    properties.getProperty("database.connectionUrl", "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1");

        } catch (IOException e){
            logger.debug("Error al leer el archivo de configuración");
        };

    }

    private void openConnection() throws SQLException {
        logger.debug("Conectando con la base de datos en " + connectionUrl);
        connection = DriverManager.getConnection(connectionUrl);

    }

    private void closeConnection()throws SQLException {
        logger.debug("Desconectando de la base de datos");
        connection.close();
    }

    private void initTables(){
        try {
            executeScript(databaseInitScript, true);
            insertarDatos();
        } catch (FileNotFoundException | SQLException e){
            logger.debug("Error inicializando la base de datos");
        }
    }

    public void executeScript(String scriptSQLFile, boolean logWriter) throws FileNotFoundException, SQLException {
        ScriptRunner sr = new ScriptRunner(connection);
        var archivo = ClassLoader.getSystemResource(scriptSQLFile).getFile();
        logger.debug("Ejecutando el script " + archivo);
        Reader reader = new BufferedReader(new FileReader(archivo));
        sr.setLogWriter(logWriter ? new PrintWriter(System.out) : null);
        sr.runScript(reader);
        closeConnection();
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                openConnection();
            } catch (SQLException e) {
                logger.error("Error al abrir la conexión con la base de datos " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    public void insertarDatos(){
        List<Funkos> funkos = new ArrayList<>();
        String csvFile = Paths.get("").toAbsolutePath() + File.separator + "data" + File.separator + "funkos.csv";
        String query = "INSERT INTO FUNKOS(cod, nombre, modelo, precio, fecha_lanzamiento) VALUES (?, ?, ?, ?, ?)";
        String line;

        try(BufferedReader br = new BufferedReader(new FileReader(csvFile));
            Connection connection = getConnection();
            PreparedStatement pstm = connection.prepareStatement(query)){
            br.readLine();
            while((line = br.readLine()) != null){
                String[] data = line.split(",");
                Funkos funko = new Funkos(
                        UUID.fromString(data[0].substring(0,35)),
                        data[1],
                        Modelo.valueOf(data[2]),
                        Double.parseDouble(data[3]),
                        LocalDate.parse(data[4]));
                funkos.add(funko);
            }

            for(Funkos funko : funkos){
                pstm.setString(1, funko.getCOD().toString());
                pstm.setString(2, funko.getNOMBRE());
                pstm.setString(3, funko.getMODELO().toString());
                pstm.setDouble(4, funko.getPRECIO());
                pstm.setString(5, String.valueOf(funko.getFECHA_LANZAMIENTO()));
                pstm.executeUpdate();
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void close() throws Exception {
        closeConnection();
    }
}
