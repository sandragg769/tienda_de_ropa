package org.example.utils;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.example.model.producto.Etiqueta;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

//tampoco herencia
public class GestorFicherosCSV {

    public static void exportarEtiquetasACSV(List<Etiqueta> listaEtiquetas, String nombreFichero) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(nombreFichero), StandardCharsets.UTF_8)) {

            StatefulBeanToCsv<Etiqueta> beanToCsv = new StatefulBeanToCsvBuilder<Etiqueta>(writer)
                    .withSeparator(';')
                    .build();

            beanToCsv.write(listaEtiquetas);

        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new RuntimeException("Error al exportar etiquetas a CSV: " + nombreFichero, e);
        }
    }

    public static List<Etiqueta> importarEtiquetasDesdeCSV(String nombreFichero) {
        try (Reader reader = new InputStreamReader(new FileInputStream(nombreFichero), StandardCharsets.UTF_8)) {

            CsvToBean<Etiqueta> csvToBean = new CsvToBeanBuilder<Etiqueta>(reader)
                    .withType(Etiqueta.class)
                    .withSeparator(';')
                    .build();

            return csvToBean.parse();

        } catch (IOException e) {
            throw new RuntimeException("Error al importar etiquetas desde CSV: " + nombreFichero, e);
        }
    }
}
