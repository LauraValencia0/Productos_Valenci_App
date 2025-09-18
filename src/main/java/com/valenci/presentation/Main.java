package com.valenci.presentation;

import com.valenci.domain.*;
import com.valenci.repository.*;
import com.valenci.service.*;
import com.valenci.util.DatabaseConnection;

import java.sql.Connection;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        // --- 1. ENSAMBLAJE DE LA APLICACIÓN (COMPOSITION ROOT) ---
        Connection connection = DatabaseConnection.getConnection();

        // Crear instancias de los repositorios
        IUsuarioRepository usuarioRepository = new UsuarioRepositoryImpl(connection);
        IProductoRepository productoRepository = new ProductoRepositoryImpl(connection);
        IPedidoRepository pedidoRepository = new PedidoRepositoryImpl(connection);

        // Crear instancias de los servicios, inyectando los repositorios
        IUsuarioService usuarioService = new UsuarioServiceImpl(usuarioRepository);
        IProductoService productoService = new ProductoServiceImpl(productoRepository);
        IPedidoService pedidoService = new PedidoServiceImpl(pedidoRepository, productoRepository);

        // --- 2. INICIO DE LA INTERFAZ DE USUARIO ---
        Scanner scanner = new Scanner(System.in);
        System.out.println("|||| BIENVENIDO A PRODUCTOS VALENCÍ ||||");

        while (true) {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. Iniciar Sesión");
            System.out.println("2. Ver Productos");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            int opcion = scanner.nextInt();
            scanner.nextLine(); // Consumir nueva línea

            switch (opcion) {
                case 1:
                    iniciarSesion(scanner, usuarioService);
                    break;
                case 2:
                    verProductos(productoService);
                    break;
                case 0:
                    System.out.println("Gracias por usar el sistema. ¡Hasta pronto!");
                    return;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
    }

    private static void iniciarSesion(Scanner scanner, IUsuarioService usuarioService) {
        System.out.println("\n--- INICIO DE SESIÓN ---");
        System.out.print("Correo electrónico: ");
        String correo = scanner.nextLine();
        System.out.print("Contraseña: ");
        String contrasena = scanner.nextLine();

        usuarioService.autenticarUsuario(correo, contrasena)
                .ifPresentOrElse(
                        usuario -> {
                            System.out.println("¡Inicio de sesión exitoso! Bienvenido, " + usuario.getNombre());
                            if (usuario instanceof Administrador) {
                                mostrarMenuAdmin();
                            } else if (usuario instanceof Cliente) {
                                mostrarMenuCliente();
                            }
                            // Aquí irían los menús para otros roles...
                        },
                        () -> System.out.println("Error: correo o contraseña incorrectos.")
                );
    }

    private static void verProductos(IProductoService productoService) {
        System.out.println("\n--- CATÁLOGO DE PRODUCTOS ---");
        productoService.obtenerTodosLosProductos().forEach(producto -> {
            String proveedor = (producto.getProveedor() != null) ? producto.getProveedor().getNombreEmpresa() : "N/A";
            System.out.printf("ID: %d | Nombre: %s | Precio: $%.2f | Stock: %d | Proveedor: %s%n",
                    producto.getIdProducto(),
                    producto.getNombreProducto(),
                    producto.getPrecio(),
                    producto.getCantidad(),
                    proveedor);
        });
    }

    private static void mostrarMenuAdmin() {
        System.out.println("\n--- MENÚ DE ADMINISTRADOR ---");
        System.out.println("1. Gestionar Productos");
        System.out.println("2. Ver Pedidos");
        System.out.println("0. Cerrar Sesión");
        // ... Lógica del menú de administrador ...
    }

    private static void mostrarMenuCliente() {
        System.out.println("\n--- MENÚ DEL CLIENTE ---");
        System.out.println("1. Realizar un Pedido");
        System.out.println("2. Ver Mis Pedidos");
        System.out.println("0. Cerrar Sesión");
        // ... Lógica del menú de cliente ...
    }
}