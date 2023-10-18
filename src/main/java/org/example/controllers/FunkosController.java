package org.example.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.database.DataBaseManager;
import org.example.models.Funkos;
import org.example.models.Modelo;
import org.example.repository.funkos.FunkoRepositoryImpl;
import org.example.serializers.LocalDateDeserializer;
import org.example.services.Service;

import java.io.*;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FunkosController {

    private static FunkosController instance;
    private List<Funkos> listaFunkos;

    private FunkosController() throws SQLException {
        var dbm = DataBaseManager.getInstance();
        var funkosRepoImpl = FunkoRepositoryImpl.getInstance(dbm);
        listaFunkos = funkosRepoImpl.findAll();
        procesarStreams();
        backup();
    }

    public static FunkosController getInstance() throws SQLException {
        if(instance == null){
            instance = new FunkosController();
        }
        return instance;
    }

    public void procesarStreams() {
        System.out.println("\n--- FUNKO MAS CARO ---");
        var masCaro = listaFunkos.stream()
                .max(Comparator.comparingDouble(Funkos::getPRECIO))
                .map(Funkos::getNOMBRE).orElse("");
        System.out.println(masCaro);

        System.out.println("\n--- MEDIA DE PRECIO DE FUNKOS");
        var mediaPrecio = listaFunkos.stream()
                .mapToDouble(Funkos::getPRECIO)
                .average()
                .orElse(0);
        System.out.println(mediaPrecio);

        System.out.println("\n--- FUNKOS AGRUPADOS POR MODELOS ---");
        var modelosAgrupados = listaFunkos.stream()
                .collect(Collectors.groupingBy(Funkos::getMODELO, Collectors.mapping(Funkos::getNOMBRE, Collectors.toList())));

        modelosAgrupados.forEach((modelo, nombre) ->{

            System.out.println("Modelo: " + modelo);
            nombre.forEach(System.out::println);
            System.out.println("");
        });

        System.out.println("\n--- NUMERO DE FUNKOS POR MODELO");
        var modelosNumeros = listaFunkos.stream()
                .collect(Collectors.groupingBy(Funkos::getMODELO, Collectors.mapping(Funkos::getNOMBRE, Collectors.toList())));

        modelosAgrupados.forEach((modelo, nombre) ->{
            System.out.println("Modelo: " + modelo + "\nCantidad: " + nombre.size());
            System.out.println("");
        });

        System.out.println("\n--- FUNKOS LANZADOS EN 2023 ---");
        listaFunkos.stream()
                .filter(x -> x.getFECHA_LANZAMIENTO().toString().contains("2023"))
                .map(Funkos::getNOMBRE)
                .forEach(System.out::println);

        System.out.println("\n--- NUMERO DE FUNKOS DE STITCH Y LISTADO DE ELLOS ---");
        var listaStitch = listaFunkos.stream()
                .map(Funkos::getNOMBRE)
                .filter(nombre -> nombre.contains("Stitch"))
                .toList();

        System.out.println("Numero de funkos de Stitch: " + listaStitch.size() + "\nListado: ");
        listaStitch.forEach(System.out::println);
    }

    public void backup() {
        var datosJSON = listaFunkos;
        String file = Paths.get("").toAbsolutePath() + File.separator + "data" + File.separator + "funkos.json";

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
        Gson gson = gsonBuilder.setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(datosJSON, writer);
            System.out.println("Datos exportados con éxito a " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
