package com.example.proyectocrudg3.controller;

import com.example.proyectocrudg3.model.Categoria;
import com.example.proyectocrudg3.model.Marca;
import com.example.proyectocrudg3.model.Producto;
import database.DatabaseConnection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;

public class ProductoController {

    // --- Formulario de gestión ---
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;
    @FXML private TextField txtDescripcion;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<Marca> cmbMarca;
    @FXML private DatePicker dpFecha;

    @FXML private Button btnNuevo;
    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;

    // --- Filtros y búsqueda ---
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Categoria> cmbBuscarCategoria;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TextField txtPrecioMin;
    @FXML private TextField txtPrecioMax;
    @FXML private CheckBox chkSoloExistencias;
    @FXML private ComboBox<String> cmbOrdenarPor;

    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiar;

    // --- Tabla de resultados ---
    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, String> colMarca;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Date> colFecha;

    private ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private Producto productoSeleccionado;

    @FXML
    public void initialize() {
        configurarColumnas();
        cargarCategorias();
        cargarMarcas();
        cargarDatosTabla();
        configurarSeleccion();

        // Opciones de ordenamiento
        if (cmbOrdenarPor != null) {
            cmbOrdenarPor.setItems(FXCollections.observableArrayList(
                    "Nombre (A-Z)",
                    "Nombre (Z-A)",
                    "Precio (Menor a Mayor)",
                    "Precio (Mayor a Menor)",
                    "Stock (Menor a Mayor)",
                    "Stock (Mayor a Menor)"
            ));
        }
    }

    private void configurarColumnas() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("nombreCategoria"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("nombreMarca"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha_creacion"));
    }

    private void configurarSeleccion() {
        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                productoSeleccionado = seleccionado;
                txtCodigo.setText(seleccionado.getCodigo());
                txtNombre.setText(seleccionado.getNombre());
                txtPrecio.setText(String.valueOf(seleccionado.getPrecio()));
                txtStock.setText(String.valueOf(seleccionado.getStock()));
                txtDescripcion.setText(seleccionado.getDescripcion() != null ? seleccionado.getDescripcion() : "");

                // Seleccionar automáticamente la Categoría en el ComboBox
                if (cmbCategoria != null && cmbCategoria.getItems() != null) {
                    for (Categoria cat : cmbCategoria.getItems()) {
                        if (cat.getId_categoria() == seleccionado.getId_categoria()) {
                            cmbCategoria.setValue(cat);
                            break;
                        }
                    }
                }

                // Seleccionar automáticamente la Marca en el ComboBox
                if (cmbMarca != null && cmbMarca.getItems() != null) {
                    for (Marca mar : cmbMarca.getItems()) {
                        if (mar.getId_marca() == seleccionado.getId_marca()) {
                            cmbMarca.setValue(mar);
                            break;
                        }
                    }
                }
            }
        });
    }

    public void cargarCategorias() {
        ObservableList<Categoria> listaCat = FXCollections.observableArrayList();
        String sql = "SELECT id_categoria, nombre FROM catalogos.categorias WHERE estado = 1 ORDER BY nombre ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaCat.add(new Categoria(rs.getInt("id_categoria"), rs.getString("nombre")));
            }

            if (cmbCategoria != null) cmbCategoria.setItems(listaCat);
            if (cmbBuscarCategoria != null) cmbBuscarCategoria.setItems(listaCat);

        } catch (SQLException e) {
            System.err.println("Error al cargar categorías: " + e.getMessage());
        }
    }

    public void cargarMarcas() {
        ObservableList<Marca> listaMar = FXCollections.observableArrayList();
        String sql = "SELECT id_marca, nombre FROM catalogos.marcas WHERE estado = 1 ORDER BY nombre ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                listaMar.add(new Marca(rs.getInt("id_marca"), rs.getString("nombre")));
            }

            if (cmbMarca != null) cmbMarca.setItems(listaMar);

        } catch (SQLException e) {
            System.err.println("Error al cargar marcas: " + e.getMessage());
        }
    }

    private void cargarDatosTabla() {
        listaProductos.clear();
        String sql = "SELECT p.*, c.nombre AS categoria_nombre, m.nombre AS marca_nombre " +
                "FROM catalogos.productos p " +
                "LEFT JOIN catalogos.categorias c ON p.id_categoria = c.id_categoria " +
                "LEFT JOIN catalogos.marcas m ON p.id_marca = m.id_marca " +
                "ORDER BY p.id_producto ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Producto p = new Producto();
                p.setId_producto(rs.getInt("id_producto"));
                p.setCodigo(rs.getString("codigo"));
                p.setNombre(rs.getString("nombre"));
                p.setDescripcion(rs.getString("descripcion"));
                p.setPrecio(rs.getDouble("precio"));
                p.setStock(rs.getInt("stock"));
                p.setFecha_creacion(rs.getDate("fecha_creacion"));
                p.setId_categoria(rs.getInt("id_categoria"));
                p.setId_marca(rs.getInt("id_marca"));
                p.setEstado(rs.getInt("estado"));

                p.setNombreCategoria(rs.getString("categoria_nombre"));
                p.setNombreMarca(rs.getString("marca_nombre"));

                listaProductos.add(p);
            }
            tblProductos.setItems(listaProductos);

        } catch (SQLException e) {
            System.err.println("Error al cargar productos: " + e.getMessage());
        }
    }

    @FXML
    void guardarProducto(ActionEvent event) {
        Categoria cat = cmbCategoria.getValue();
        Marca mar = cmbMarca.getValue();

        if (cat == null || mar == null) {
            mostrarAlerta("Campos requeridos", "Por favor selecciona una categoría y una marca.");
            return;
        }

        String sql = "INSERT INTO catalogos.productos (codigo, nombre, descripcion, precio, stock, fecha_creacion, id_categoria, id_marca, estado) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_DATE, ?, ?, 1)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtCodigo.getText().trim());
            pstmt.setString(2, txtNombre.getText().trim());
            pstmt.setString(3, txtDescripcion.getText().trim());
            pstmt.setDouble(4, Double.parseDouble(txtPrecio.getText().trim()));
            pstmt.setInt(5, Integer.parseInt(txtStock.getText().trim()));
            pstmt.setInt(6, cat.getId_categoria());
            pstmt.setInt(7, mar.getId_marca());

            pstmt.executeUpdate();
            cargarDatosTabla();
            nuevoProducto(null);
            mostrarAlerta("Éxito", "Producto registrado correctamente.");

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al guardar el producto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void actualizarProducto(ActionEvent event) {
        if (productoSeleccionado == null) {
            mostrarAlerta("Aviso", "Selecciona un producto de la tabla para actualizar.");
            return;
        }

        Categoria cat = cmbCategoria.getValue();
        Marca mar = cmbMarca.getValue();

        if (cat == null || mar == null) {
            mostrarAlerta("Campos requeridos", "Debes seleccionar una categoría y una marca.");
            return;
        }

        String sql = "UPDATE catalogos.productos SET codigo = ?, nombre = ?, descripcion = ?, precio = ?, stock = ?, id_categoria = ?, id_marca = ? WHERE id_producto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtCodigo.getText().trim());
            pstmt.setString(2, txtNombre.getText().trim());
            pstmt.setString(3, txtDescripcion.getText().trim());
            pstmt.setDouble(4, Double.parseDouble(txtPrecio.getText().trim()));
            pstmt.setInt(5, Integer.parseInt(txtStock.getText().trim()));
            pstmt.setInt(6, cat.getId_categoria());
            pstmt.setInt(7, mar.getId_marca());
            pstmt.setInt(8, productoSeleccionado.getId_producto());

            pstmt.executeUpdate();
            cargarDatosTabla();
            nuevoProducto(null);
            mostrarAlerta("Éxito", "Producto actualizado correctamente.");

        } catch (Exception e) {
            mostrarAlerta("Error", "Error al actualizar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void eliminarProducto(ActionEvent event) {
        if (productoSeleccionado == null) {
            mostrarAlerta("Aviso", "Selecciona un producto de la tabla para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Seguro que deseas eliminar este producto?", ButtonType.YES, ButtonType.NO);
        confirmacion.showAndWait();

        if (confirmacion.getResult() == ButtonType.YES) {
            String sql = "DELETE FROM catalogos.productos WHERE id_producto = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, productoSeleccionado.getId_producto());
                pstmt.executeUpdate();

                cargarDatosTabla();
                nuevoProducto(null);
                mostrarAlerta("Éxito", "Producto eliminado con éxito.");

            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    void nuevoProducto(ActionEvent event) {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtDescripcion.clear();
        if (cmbCategoria != null) cmbCategoria.setValue(null);
        if (cmbMarca != null) cmbMarca.setValue(null);
        if (dpFecha != null) dpFecha.setValue(null);
        productoSeleccionado = null;
        tblProductos.getSelectionModel().clearSelection();
    }

    @FXML
    void buscarProductos(ActionEvent event) {
        String criterio = txtBuscar.getText() != null ? txtBuscar.getText().trim().toLowerCase() : "";
        Categoria catFiltro = cmbBuscarCategoria != null ? cmbBuscarCategoria.getValue() : null;

        ObservableList<Producto> filtrados = FXCollections.observableArrayList();

        for (Producto p : listaProductos) {
            boolean coincideTexto = criterio.isEmpty() ||
                    (p.getNombre() != null && p.getNombre().toLowerCase().contains(criterio)) ||
                    (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(criterio));

            boolean coincideCategoria = (catFiltro == null) || (p.getId_categoria() == catFiltro.getId_categoria());

            boolean coincideExistencia = true;
            if (chkSoloExistencias != null && chkSoloExistencias.isSelected()) {
                coincideExistencia = p.getStock() > 0;
            }

            if (coincideTexto && coincideCategoria && coincideExistencia) {
                filtrados.add(p);
            }
        }

        tblProductos.setItems(filtrados);
    }

    @FXML
    void limpiarFiltros(ActionEvent event) {
        if (txtBuscar != null) txtBuscar.clear();
        if (cmbBuscarCategoria != null) cmbBuscarCategoria.setValue(null);
        if (dpDesde != null) dpDesde.setValue(null);
        if (dpHasta != null) dpHasta.setValue(null);
        if (txtPrecioMin != null) txtPrecioMin.clear();
        if (txtPrecioMax != null) txtPrecioMax.clear();
        if (chkSoloExistencias != null) chkSoloExistencias.setSelected(false);
        if (cmbOrdenarPor != null) cmbOrdenarPor.setValue(null);

        tblProductos.setItems(listaProductos);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}