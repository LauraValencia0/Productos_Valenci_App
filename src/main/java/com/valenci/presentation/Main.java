package com.valenci.presentation;

import com.valenci.domain.*;
import com.valenci.repository.*;
import com.valenci.service.*;
import com.valenci.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Connection connection = DatabaseConnection.getConnection();
        IUsuarioRepository usuarioRepository = new UsuarioRepositoryImpl(connection);
        IProductoRepository productoRepository = new ProductoRepositoryImpl(connection);
        IPedidoRepository pedidoRepository = new PedidoRepositoryImpl(connection);
        IUsuarioService usuarioService = new UsuarioServiceImpl(usuarioRepository);
        IProductoService productoService = new ProductoServiceImpl(productoRepository);
        IPedidoService pedidoService = new PedidoServiceImpl(pedidoRepository, productoRepository);

        inicializarAdmin(usuarioService);

        Scanner scanner = new Scanner(System.in);
        System.out.println("|||| BIENVENIDO A PRODUCTOS VALENCÍ ||||");

        while (true) {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. Portal de Usuario (Ingresar o Registrarse)");
            System.out.println("2. Ver Catálogo de Productos");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            try {
                int opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1:
                        manejarPortalAutenticacion(scanner, usuarioService, productoService, pedidoService);
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
            } catch (NumberFormatException e) {
                System.out.println("Error: Por favor, ingrese un número válido.");
            }
        }
    }

    private static void manejarPortalAutenticacion(Scanner scanner, IUsuarioService usuarioService, IProductoService productoService, IPedidoService pedidoService) {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- PORTAL DE USUARIO ---");
            System.out.println("1. Ingresar con cuenta existente");
            System.out.println("2. Crear nueva cuenta (Cliente)");
            System.out.println("0. Volver al menú principal");
            System.out.print("Seleccione una opción: ");

            try {
                int opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1:
                        autenticarUsuarioExistente(scanner, usuarioService, productoService, pedidoService);
                        break;
                    case 2:
                        registrarNuevoCliente(scanner, usuarioService);
                        break;
                    case 0:
                        volver = true;
                        break;
                    default:
                        System.out.println("Opción no válida. Intente de nuevo.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Por favor, ingrese un número válido.");
            }
        }
    }

    private static void registrarNuevoCliente(Scanner scanner, IUsuarioService usuarioService) {
        try {
            System.out.println("\n--- REGISTRO DE NUEVO CLIENTE ---");
            System.out.print("Nombre completo: ");
            String nombre = scanner.nextLine();
            System.out.print("Correo electrónico: ");
            String correo = scanner.nextLine();
            System.out.print("Contraseña: ");
            String contrasena = scanner.nextLine();
            System.out.print("Dirección de Envío: ");
            String direccion = scanner.nextLine();

            Cliente nuevoCliente = new Cliente(0, nombre, correo, contrasena, direccion);
            usuarioService.registrarUsuario(nuevoCliente);
            System.out.println("¡Cliente registrado exitosamente! Ahora puede iniciar sesión.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error en el registro: " + e.getMessage());
        }
    }

    private static void autenticarUsuarioExistente(Scanner scanner, IUsuarioService usuarioService, IProductoService productoService, IPedidoService pedidoService) {
        System.out.println("\n--- INICIO DE SESIÓN ---");
        System.out.print("Correo electrónico: ");
        String correo = scanner.nextLine();
        System.out.print("Contraseña: ");
        String contrasena = scanner.nextLine();

        usuarioService.autenticarUsuario(correo, contrasena)
                .ifPresentOrElse(
                        usuario -> {
                            System.out.println("\n¡Inicio de sesión exitoso! Bienvenido, " + usuario.getNombre());
                            if (usuario instanceof Administrador) {
                                iniciarSesionAdmin(scanner, (Administrador) usuario, productoService, pedidoService, usuarioService);
                            } else if (usuario instanceof Cliente) {
                                iniciarSesionCliente(scanner, (Cliente) usuario, productoService, pedidoService);
                            }
                        },
                        () -> System.out.println("\nError: correo o contraseña incorrectos.")
                );
    }

    private static void iniciarSesionAdmin(Scanner scanner, Administrador admin, IProductoService productoService, IPedidoService pedidoService, IUsuarioService usuarioService) {
        boolean cerrarSesion = false;
        while (!cerrarSesion) {
            System.out.println("\n--- MENÚ DE ADMINISTRADOR ---");
            System.out.println("1. Gestionar Productos");
            System.out.println("2. Ver Todos los Pedidos");
            System.out.println("3. Gestionar Proveedores");
            System.out.println("0. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");

            try {
                int opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1:
                        gestionarProductos(scanner, productoService, usuarioService);
                        break;
                    case 2:
                        verTodosLosPedidos(pedidoService);
                        break;
                    case 3:
                        gestionarProveedores(scanner, usuarioService);
                        break;
                    case 0:
                        cerrarSesion = true;
                        System.out.println("Cerrando sesión...");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Por favor, ingrese un número válido.");
            }
        }
    }

    private static void gestionarProveedores(Scanner scanner, IUsuarioService usuarioService) {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTIÓN DE PROVEEDORES ---");
            System.out.println("1. Crear Nuevo Proveedor");
            System.out.println("2. Listar Todos los Proveedores");
            System.out.println("0. Volver al Menú de Administrador");
            System.out.print("Seleccione una opción: ");

            try {
                int opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1:
                        System.out.println("\n-- Creando Nuevo Proveedor --");
                        System.out.print("Nombre del contacto: ");
                        String nombre = scanner.nextLine();
                        System.out.print("Correo electrónico: ");
                        String correo = scanner.nextLine();
                        System.out.print("Contraseña: ");
                        String contrasena = scanner.nextLine();
                        System.out.print("Nombre de la empresa: ");
                        String empresa = scanner.nextLine();

                        Proveedor nuevoProveedor = new Proveedor(0, nombre, correo, contrasena, empresa);
                        usuarioService.registrarUsuario(nuevoProveedor);
                        System.out.println("¡Proveedor '" + empresa + "' registrado exitosamente!");
                        break;
                    case 2:
                        System.out.println("\n-- Listado de Proveedores --");
                        usuarioService.obtenerTodosLosUsuarios().stream()
                                .filter(usuario -> usuario instanceof Proveedor)
                                .forEach(usuario -> {
                                    Proveedor p = (Proveedor) usuario;
                                    System.out.printf("ID: %d | Contacto: %s | Empresa: %s | Correo: %s%n",
                                            p.getId(), p.getNombre(), p.getNombreEmpresa(), p.getCorreo());
                                });
                        break;
                    case 0:
                        volver = true;
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void gestionarProductos(Scanner scanner, IProductoService productoService, IUsuarioService usuarioService) {
        System.out.println("\n--- GESTIÓN DE PRODUCTOS ---");
        System.out.println("1. Crear nuevo producto");
        System.out.println("2. Actualizar un producto");
        // ... más opciones como eliminar, etc.
        System.out.print("Seleccione una opción: ");
        int opcion = Integer.parseInt(scanner.nextLine());

        if (opcion == 1) {
            try {
                System.out.print("Nombre del producto: ");
                String nombre = scanner.nextLine();
                System.out.print("Descripción: ");
                String desc = scanner.nextLine();
                System.out.print("Precio (ej. 25000.50): ");
                BigDecimal precio = new BigDecimal(scanner.nextLine());
                System.out.print("Cantidad en stock: ");
                int stock = Integer.parseInt(scanner.nextLine());
                System.out.print("ID del Proveedor: ");
                int idProveedor = Integer.parseInt(scanner.nextLine());

                Usuario proveedorUsuario = usuarioService.buscarUsuarioPorId(idProveedor)
                        .filter(u -> u instanceof Proveedor)
                        .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado o el ID no corresponde a un proveedor."));

                Producto nuevoProducto = new Producto(0, nombre, desc, precio, stock, (Proveedor) proveedorUsuario);
                productoService.agregarProducto(nuevoProducto);
                System.out.println("Producto '" + nombre + "' creado exitosamente.");

            } catch (Exception e) {
                System.out.println("Error al crear el producto: " + e.getMessage());
            }
        }
    }

    private static void verTodosLosPedidos(IPedidoService pedidoService) {
        System.out.println("\n--- HISTORIAL GLOBAL DE PEDIDOS ---");
        List<Pedido> pedidos = pedidoService.obtenerTodosLosPedidos();
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos registrados en el sistema.");
            return;
        }
        pedidos.forEach(pedido -> {
            System.out.printf("ID Pedido: %d | Fecha: %s | Cliente: %s | Total: $%.2f | Estado: %s%n",
                    pedido.getIdPedido(),
                    pedido.getFechaPedido().toLocalDate(),
                    pedido.getCliente().getNombre(),
                    pedido.getTotalPedido(),
                    pedido.getEstadoPedido());
        });
    }


    private static void iniciarSesionCliente(Scanner scanner, Cliente cliente, IProductoService productoService, IPedidoService pedidoService) {
        boolean cerrarSesion = false;
        while (!cerrarSesion) {
            System.out.println("\n--- MENÚ DEL CLIENTE ---");
            System.out.println("1. Realizar un Pedido");
            System.out.println("2. Ver Mis Pedidos");
            System.out.println("0. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");

            try {
                int opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1:
                        realizarPedido(scanner, cliente, productoService, pedidoService);
                        break;
                    case 2:
                        verMisPedidos(cliente, pedidoService);
                        break;
                    case 0:
                        cerrarSesion = true;
                        System.out.println("Cerrando sesión...");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Por favor, ingrese un número válido.");
            }
        }
    }

    private static void realizarPedido(Scanner scanner, Cliente cliente, IProductoService productoService, IPedidoService pedidoService) {
        System.out.println("\n--- REALIZAR NUEVO PEDIDO ---");
        verProductos(productoService);

        List<DetallePedido> detalles = new ArrayList<>();
        while (true) {
            System.out.print("\nIngrese el ID del producto que desea agregar (o 0 para finalizar): ");
            int idProducto = Integer.parseInt(scanner.nextLine());
            if (idProducto == 0) break;

            System.out.print("Ingrese la cantidad: ");
            int cantidad = Integer.parseInt(scanner.nextLine());

            Optional<Producto> productoOpt = productoService.obtenerProductoPorId(idProducto);
            if (productoOpt.isPresent()) {
                DetallePedido detalle = new DetallePedido();
                detalle.setProducto(productoOpt.get());
                detalle.setCantidad(cantidad);
                detalles.add(detalle);
                System.out.println("Producto agregado al carrito.");
            } else {
                System.out.println("Producto no encontrado.");
            }
        }

        if (detalles.isEmpty()) {
            System.out.println("Carrito vacío. Pedido cancelado.");
            return;
        }

        try {
            Pedido nuevoPedido = new Pedido();
            nuevoPedido.setCliente(cliente);
            nuevoPedido.setDetalles(detalles);

            Pedido pedidoCreado = pedidoService.crearPedido(nuevoPedido);
            System.out.println("\n¡PEDIDO CREADO EXITOSAMENTE!");
            System.out.println("Resumen del Pedido:");
            System.out.println("ID de Pedido: " + pedidoCreado.getIdPedido());
            System.out.println("Fecha: " + pedidoCreado.getFechaPedido().toLocalDate());
            System.out.println("Total: $" + pedidoCreado.getTotalPedido());
            System.out.println("Estado: " + pedidoCreado.getEstadoPedido());
        } catch (Exception e) {
            System.out.println("\nError al crear el pedido: " + e.getMessage());
        }
    }

    private static void verMisPedidos(Cliente cliente, IPedidoService pedidoService) {
        System.out.println("\n--- MIS PEDIDOS ---");
        List<Pedido> misPedidos = pedidoService.obtenerPedidosPorCliente(cliente.getId());
        if (misPedidos.isEmpty()) {
            System.out.println("No has realizado ningún pedido todavía.");
            return;
        }
        misPedidos.forEach(pedido -> {
            System.out.printf("ID Pedido: %d | Fecha: %s | Total: $%.2f | Estado: %s%n",
                    pedido.getIdPedido(),
                    pedido.getFechaPedido().toLocalDate(),
                    pedido.getTotalPedido(),
                    pedido.getEstadoPedido());
        });
    }


    private static void inicializarAdmin(IUsuarioService usuarioService) {
        String adminCorreo = "admin@valenci.com";
        if (usuarioService.buscarUsuarioPorCorreo(adminCorreo).isEmpty()) {
            System.out.println("Creando cuenta de administrador por defecto...");
            Administrador nuevoAdmin = new Administrador();
            nuevoAdmin.setNombre("Admin Principal");
            nuevoAdmin.setCorreo(adminCorreo);
            nuevoAdmin.setContrasena("admin123");
            usuarioService.registrarUsuario(nuevoAdmin);
        }
    }

    private static void verProductos(IProductoService productoService) {
        System.out.println("\n--- CATÁLOGO DE PRODUCTOS ---");
        productoService.obtenerTodosLosProductos().forEach(producto -> {
            System.out.printf("ID: %d | Nombre: %s | Precio: $%.2f | Stock: %d%n",
                    producto.getIdProducto(),
                    producto.getNombreProducto(),
                    producto.getPrecio(),
                    producto.getCantidad());
        });
    }
}