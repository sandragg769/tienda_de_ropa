package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConf {
    //variables estáticas y finales comunes para usar
    public static final String USER = "root";
    public static final String PASS = "#Proyecto2526";
    public static final String URL = "jdbc:mysql://localhost:3306/sanscloset";

    //sentencias de creación de tablas
    private static final String crearTablaUsuario =
            "CREATE TABLE IF NOT EXISTS usuario (" +
                    " id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    " dni VARCHAR(20) UNIQUE NOT NULL," +
                    " nombre VARCHAR(20) NOT NULL," +
                    " direccion VARCHAR(255)," +
                    " fecha_nacimiento DATE NOT NULL," +
                    " telefono VARCHAR(50)," +
                    " email VARCHAR(255) UNIQUE NOT NULL," +
                    " password VARCHAR(255) NOT NULL" +
                    //la relación de usuario-pedido está en la tabla pedido
                    //la relación de usuario-producto es otra tabla específica de usuariosProductosFavoritos
                    ");";

    private static final String crearTablaEtiqueta =
            "CREATE TABLE IF NOT EXISTS etiqueta (" +
                    " id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    " nombre VARCHAR(100) NOT NULL," +
                    " fecha_creacion DATE NOT NULL" +
                    //la relación etiqueta-producto está en la tabla producto
                    ");";

    //single table para productos
    private static final String crearTablaProducto =
            "CREATE TABLE IF NOT EXISTS producto (" +
                    " id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    //añadir el tipo por la herencia
                    " tipo VARCHAR(50) NOT NULL," +
                    " nombre VARCHAR(255) NOT NULL," +
                    " marca VARCHAR(255) NOT NULL," +
                    " precio_inicial DOUBLE NOT NULL," +
                    //la talla y el color son enum
                    " talla ENUM('XS', 'S', 'M', 'L', 'XL', 'XXL') NOT NULL," +
                    " color ENUM('AZUL', 'ROSA', 'ROJO', 'MARRON', 'BEIGE', 'BLANCO', 'NEGRO', 'VERDE', 'AMARILLO') NOT NULL," +
                    //crear campo para el fk con etiqueta
                    " etiqueta_id BIGINT," +
                    //poner el tipo por la herencia, no hace falta hacer una tabla
                    " descuento_tipo VARCHAR(50)," +
                    " descuento_valor DOUBLE," +
                    " botones INT," +
                    " bolsillos INT," +
                    " con_capucha BOOLEAN," +
                    " nivel_abrigo INT," +
                    //relación de producto- etiqueta (un producto tiene una etiqueta, una etiqueta puede tenerla muchos productos)
                    //se hace SET NULL para que si borramos las etiquetas no se borren los productos, así los productos siguen existiendo pero quedan sin clasificación
                    " FOREIGN KEY (etiqueta_id) REFERENCES etiqueta(id) ON DELETE SET NULL" +
                    ");";

    private static final String crearTablaPedido =
            "CREATE TABLE IF NOT EXISTS pedido (" +
                    " id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    " fecha DATETIME NOT NULL," +
                    " estado VARCHAR(20) NOT NULL," +
                    //crear el campo para hacerlo fk
                    " usuario_id BIGINT NOT NULL," +
                    //relación pedido-usuario (un usuario tiene muchos pedidos, pero un pedido solo puede ser de un usuario)
                    " FOREIGN KEY (usuario_id) REFERENCES usuario(id) " +
                    //delete y update en cascada (así se indica en el enunciado)
                    " ON DELETE CASCADE ON UPDATE CASCADE" +
                    //la relación con línea de pedido está en la tabla lineaPedido
                    ");";

    private static final String crearTablaLineaPedido =
            "CREATE TABLE IF NOT EXISTS linea_pedido (" +
                    " id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    " cantidad INT NOT NULL," +
                    //crear campo para la relación con producto
                    " producto_id BIGINT NOT NULL," +
                    //crear campo para la relación con pedido
                    " pedido_id BIGINT NOT NULL," +
                    //relación lineaPedido-producto (en una línea de pedido solo hay un producto y un producto puede estar en muchas líneas de pedido)
                    //poner RESCRICT para no borrar un producto al borrar una línea de pedido
                    " FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE RESTRICT ON UPDATE CASCADE," +
                    //relación lineaPedido-pedido (una línea de pedido específica es de un pedido y un pedido tiene muchas líneas de pedido)
                    " FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ");";

    private static final String crearTablaUsuarioProductoFavorito =
            "CREATE TABLE IF NOT EXISTS usuario_producto_favorito (" +
                    //crearlos para el fk
                    " usuario_id BIGINT NOT NULL," +
                    " producto_id BIGINT NOT NULL," +
                    //asignar como pk los dos campos juntos
                    " PRIMARY KEY (usuario_id, producto_id)," +
                    //relación usuario-producto (un usuario puede tener 0 o muchos productos favoritos y un producto puede ser favorito de 0 usuarios o muchos)
                    " FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE CASCADE," +
                    " FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE ON UPDATE CASCADE" +
                    ");";

    //sentencias para borrar tablas
    private static final String borrarTablaUsuarioProductoFavorito = "DROP TABLE IF EXISTS usuario_producto_favorito;";
    private static final String borrarTablaLineaPedido = "DROP TABLE IF EXISTS linea_pedido;";
    private static final String borrarTablaPedido = "DROP TABLE IF EXISTS pedido;";
    private static final String borrarTablaProducto = "DROP TABLE IF EXISTS producto;";
    private static final String borrarTablaEtiqueta = "DROP TABLE IF EXISTS etiqueta;";
    private static final String borrarTablaUsuario = "DROP TABLE IF EXISTS usuario;";

    //metodo para arrancar
    public static void createTables() throws SQLException {
        //abre conexión con la base de datos
        try (Connection connection = DriverManager.getConnection(URL, USER, PASS);
             //crea un statement que sirve para ejecutar comandos SQL fijos (sin parámetros)
             Statement statement = connection.createStatement()) {
            //usar try with resources para que se cierre automáticamente
            //después crear las tablas
            statement.executeUpdate(crearTablaUsuario);
            statement.executeUpdate(crearTablaEtiqueta);
            statement.executeUpdate(crearTablaProducto);
            statement.executeUpdate(crearTablaPedido);
            statement.executeUpdate(crearTablaLineaPedido);
            statement.executeUpdate(crearTablaUsuarioProductoFavorito);
        }
    }

    //metodo para reiniciar la base de cero
    public static void dropAndCreateTables() throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASS);
             Statement statement = connection.createStatement()) {

            //importante borrar en orden inverso a las dependencias
            statement.executeUpdate(borrarTablaUsuarioProductoFavorito);
            statement.executeUpdate(borrarTablaLineaPedido);
            statement.executeUpdate(borrarTablaPedido);
            statement.executeUpdate(borrarTablaProducto);
            statement.executeUpdate(borrarTablaEtiqueta);
            statement.executeUpdate(borrarTablaUsuario);
            //volver a crearlas
            createTables();
        }
    }
}
