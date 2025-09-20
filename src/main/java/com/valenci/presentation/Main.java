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
        IPagoRepository pagoRepository = new PagoRepositoryImpl(connection);

        IUsuarioService usuarioService = new UsuarioServiceImpl(usuarioRepository);
        IProductoService productoService = new ProductoServiceImpl(productoRepository);

        IFacturaRepository facturaRepository = new FacturaRepositoryImpl(connection);

        IPedidoService pedidoService = new PedidoServiceImpl(pedidoRepository, productoRepository, pagoRepository, facturaRepository);

        // --- INICIALIZACIÓN ---
        inicializarAdmin(usuarioService);

        // --- INTERFAZ DE USUARIO ---
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
                        mostrarCatalogoCompleto(productoService);
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
            System.out.println("2. Gestionar Pedidos");
            System.out.println("3. Gestionar Proveedores");
            System.out.println("4. Gestionar Facturas"); // <-- NUEVA OPCIÓN
            System.out.println("0. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");

            try {
                int opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1:
                        gestionarProductos(scanner, productoService, usuarioService);
                        break;
                    case 2:
                        gestionarPedidos(scanner, pedidoService, productoService);
                        break;
                    case 3:
                        gestionarProveedores(scanner, usuarioService);
                        break;
                    case 4: // <-- NUEVO CASE
                        gestionarFacturas(scanner, pedidoService, usuarioService);
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
            System.out.println("3. Actualizar un Proveedor"); // <-- Nueva opción
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
                    case 3:
                        // --- LÓGICA NUEVA PARA ACTUALIZAR ---
                        System.out.println("\n-- Actualizando Proveedor --");
                        System.out.print("Ingrese el ID del proveedor a actualizar: ");
                        int idProveedor = Integer.parseInt(scanner.nextLine());

                        // Buscamos y validamos que el usuario sea un proveedor
                        Usuario usuarioAActualizar = usuarioService.buscarUsuarioPorId(idProveedor)
                                .filter(u -> u instanceof Proveedor)
                                .orElseThrow(() -> new IllegalArgumentException("ID no encontrado o no corresponde a un proveedor."));

                        Proveedor proveedorAActualizar = (Proveedor) usuarioAActualizar;

                        System.out.println("Editando proveedor: " + proveedorAActualizar.getNombreEmpresa());
                        System.out.println("Deje el campo en blanco para no modificar el valor actual.");

                        System.out.print("Nuevo nombre de contacto (" + proveedorAActualizar.getNombre() + "): ");
                        String nuevoNombre = scanner.nextLine();
                        if (!nuevoNombre.isBlank()) proveedorAActualizar.setNombre(nuevoNombre);

                        System.out.print("Nuevo correo (" + proveedorAActualizar.getCorreo() + "): ");
                        String nuevoCorreo = scanner.nextLine();
                        if (!nuevoCorreo.isBlank()) proveedorAActualizar.setCorreo(nuevoCorreo);

                        System.out.print("Nuevo nombre de empresa (" + proveedorAActualizar.getNombreEmpresa() + "): ");
                        String nuevaEmpresa = scanner.nextLine();
                        if (!nuevaEmpresa.isBlank()) proveedorAActualizar.setNombreEmpresa(nuevaEmpresa);

                        // Llamamos al nuevo método del servicio para actualizar
                        usuarioService.actualizarUsuario(proveedorAActualizar);
                        System.out.println("¡Proveedor actualizado exitosamente!");
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

    // --- MÉTODO CORREGIDO Y COMPLETADO ---
    private static void gestionarProductos(Scanner scanner, IProductoService productoService, IUsuarioService usuarioService) {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTIÓN DE PRODUCTOS ---");
            System.out.println("1. Crear nuevo producto");
            System.out.println("2. Actualizar un producto");
            System.out.println("3. Eliminar un producto");
            System.out.println("4. Ver todos los productos");
            System.out.println("0. Volver al menú de administrador");
            System.out.print("Seleccione una opción: ");

            try {
                int opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1:
                        // Lógica para crear producto
                        System.out.println("\n-- Creando Nuevo Producto --");
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
                        break;
                    case 2:
                        // Lógica para actualizar producto
                        System.out.println("\n-- Actualizando Producto --");
                        System.out.print("Ingrese el ID del producto a actualizar: ");
                        int idActualizar = Integer.parseInt(scanner.nextLine());

                        // Busca el producto para obtener sus datos actuales
                        Producto productoAActualizar = productoService.obtenerProductoPorId(idActualizar)
                                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

                        System.out.println("Editando producto: " + productoAActualizar.getNombreProducto());
                        System.out.println("Deje el campo en blanco para no modificar el valor actual.");

                        System.out.print("Nuevo nombre (" + productoAActualizar.getNombreProducto() + "): ");
                        String nuevoNombre = scanner.nextLine();
                        if (!nuevoNombre.isBlank()) productoAActualizar.setNombreProducto(nuevoNombre);

                        System.out.print("Nueva descripción (" + productoAActualizar.getDescripcion() + "): ");
                        String nuevaDesc = scanner.nextLine();
                        if (!nuevaDesc.isBlank()) productoAActualizar.setDescripcion(nuevaDesc);

                        System.out.print("Nuevo precio (" + productoAActualizar.getPrecio() + "): ");
                        String nuevoPrecioStr = scanner.nextLine();
                        if (!nuevoPrecioStr.isBlank()) productoAActualizar.setPrecio(new BigDecimal(nuevoPrecioStr));

                        System.out.print("Nueva cantidad en stock (" + productoAActualizar.getCantidad() + "): ");
                        String nuevoStockStr = scanner.nextLine();
                        if (!nuevoStockStr.isBlank()) productoAActualizar.setCantidad(Integer.parseInt(nuevoStockStr));

                        // Llamamos al servicio para que guarde los cambios
                        productoService.actualizarProducto(productoAActualizar);
                        System.out.println("¡Producto actualizado exitosamente!");
                        break;
                    case 3:
                        // Lógica para eliminar
                        System.out.println("\n-- Eliminando Producto --");
                        System.out.print("Ingrese el ID del producto a eliminar: ");
                        int idEliminar = Integer.parseInt(scanner.nextLine());
                        productoService.eliminarProducto(idEliminar);
                        System.out.println("Producto con ID " + idEliminar + " eliminado exitosamente.");
                        break;
                    case 4:
                        mostrarCatalogoCompleto(productoService);
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

    private static void verTodosLosPedidos(IPedidoService pedidoService) {
        System.out.println("\n--- HISTORIAL GLOBAL DE PEDIDOS ---");
        List<Pedido> pedidos = pedidoService.obtenerTodosLosPedidos();
        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos registrados en el sistema.");
            return;
        }

        // Iteramos sobre cada pedido para mostrarlo
        for (Pedido pedido : pedidos) {
            // Imprimimos el resumen del pedido como antes
            imprimirResumenPedido(pedido, true);

            // Y ahora, imprimimos los detalles de ESE pedido
            if (pedido.getDetalles() != null && !pedido.getDetalles().isEmpty()) {
                System.out.println("  Detalles del Pedido:");
                for (DetallePedido detalle : pedido.getDetalles()) {
                    System.out.printf("    - Producto: %s | Cantidad: %d | Precio Unit.: $%.2f | Subtotal: $%.2f%n",
                            detalle.getProducto().getNombreProducto(),
                            detalle.getCantidad(),
                            detalle.getPrecioUnitario(),
                            detalle.getSubtotal()
                    );
                }
            }
            System.out.println("----------------------------------------");
        }
    }

    private static void iniciarSesionCliente(Scanner scanner, Cliente cliente, IProductoService productoService, IPedidoService pedidoService) {
        boolean cerrarSesion = false;
        while (!cerrarSesion) {
            System.out.println("\n--- MENÚ DEL CLIENTE ---");
            System.out.println("1. Realizar un Pedido");
            System.out.println("2. Ver Mis Pedidos");
            System.out.println("3. Pagar Pedido Pendiente"); // <-- NUEVA OPCIÓN
            System.out.println("0. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");

            try {
                int opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1:
                        realizarPedido(scanner, cliente, productoService, pedidoService);
                        break;
                    case 2:
                        verMisPedidos(scanner, cliente, pedidoService);
                        break;
                    case 3:
                        pagarPedido(scanner, cliente, pedidoService); // <-- NUEVA LLAMADA
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
        mostrarCatalogoCompleto(productoService);

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
            imprimirResumenPedido(pedidoCreado, false);
        } catch (Exception e) {
            System.out.println("\nError al crear el pedido: " + e.getMessage());
        }
    }

    private static void pagarPedido(Scanner scanner, Cliente cliente, IPedidoService pedidoService) {
        System.out.println("\n--- PAGAR PEDIDO PENDIENTE ---");
        System.out.println("Tus pedidos pendientes de pago:");

        // Filtramos y mostramos solo los pedidos pendientes del cliente
        List<Pedido> pedidosPendientes = pedidoService.obtenerPedidosPorCliente(cliente.getId())
                .stream()
                .filter(p -> p.getEstadoPedido() == EstadoPedido.PENDIENTE)
                .toList();

        if (pedidosPendientes.isEmpty()) {
            System.out.println("No tienes pedidos pendientes de pago.");
            return;
        }

        pedidosPendientes.forEach(p -> imprimirResumenPedido(p, false));

        try {
            System.out.print("\nIngrese el ID del pedido que desea pagar: ");
            int idPedido = Integer.parseInt(scanner.nextLine());

            // Verificamos que el pedido a pagar le pertenezca y esté en la lista
            Pedido pedidoAPagar = pedidosPendientes.stream()
                    .filter(p -> p.getIdPedido() == idPedido)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("El ID no corresponde a uno de tus pedidos pendientes."));

            System.out.println("Total a pagar por el pedido " + idPedido + ": $" + pedidoAPagar.getTotalPedido());
            System.out.println("Seleccione el método de pago:");
            System.out.println("1. TARJETA_CREDITO");
            System.out.println("2. TARJETA_DEBITO");
            System.out.println("3. TRANSFERENCIA");
            int metodoOpcion = Integer.parseInt(scanner.nextLine());

            MetodoPago metodoPago;
            switch (metodoOpcion) {
                case 1: metodoPago = MetodoPago.TARJETA_CREDITO; break;
                case 2: metodoPago = MetodoPago.TARJETA_DEBITO; break;
                case 3: metodoPago = MetodoPago.TRANSFERENCIA; break;
                default: System.out.println("Método no válido."); return;
            }

            // Llamada al servicio para procesar el pago
            pedidoService.registrarPagoParaPedido(idPedido, pedidoAPagar.getTotalPedido(), metodoPago);

        } catch (Exception e) {
            System.out.println("\nError al procesar el pago: " + e.getMessage());
        }
    }

    // Metodo modificado para ver los pedidos de cliente más detallados

    private static void verMisPedidos(Scanner scanner, Cliente cliente, IPedidoService pedidoService) {
        System.out.println("\n--- MIS PEDIDOS ---");
        List<Pedido> misPedidos = pedidoService.obtenerPedidosPorCliente(cliente.getId());
        if (misPedidos.isEmpty()) {
            System.out.println("No has realizado ningún pedido todavía.");
            return;
        }

        for (Pedido pedido : misPedidos) {
            // Imprimir el resumen del pedido
            imprimirResumenPedido(pedido, false);
            //  imprimir detalles pedidos Cliente
            if (pedido.getDetalles() != null && !pedido.getDetalles().isEmpty()) {
                System.out.println("  Detalles:");
                for (DetallePedido detalle : pedido.getDetalles()) {
                    System.out.printf("    - Producto: %s | Cantidad: %d | Subtotal: $%.2f%n",
                            detalle.getProducto().getNombreProducto(),
                            detalle.getCantidad(),
                            detalle.getSubtotal()
                    );
                }
            }
            System.out.println("----------------------------------------");
        }
        System.out.print("\nIngrese el ID de un pedido para ver su factura (o 0 para volver): ");
        try {
            int idPedido = Integer.parseInt(scanner.nextLine());
            if (idPedido == 0) {
                return;
            }

            pedidoService.obtenerFacturaPorPedidoId(idPedido)
                    .ifPresentOrElse(
                            factura -> mostrarDetalleFactura(factura, cliente.getNombre()),
                            () -> System.out.println("Este pedido no tiene una factura asociada o el ID es incorrecto.")
                    );

        } catch (NumberFormatException e) {
            System.out.println("ID no válido.");
        }
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

    private static void mostrarCatalogoCompleto(IProductoService productoService) {
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

    private static void imprimirResumenPedido(Pedido pedido, boolean mostrarCliente) {
        if (mostrarCliente) {
            System.out.printf("ID Pedido: %d | Fecha: %s | Cliente: %s | Total: $%.2f | Estado: %s%n",
                    pedido.getIdPedido(),
                    pedido.getFechaPedido().toLocalDate(),
                    pedido.getCliente().getNombre(),
                    pedido.getTotalPedido(),
                    pedido.getEstadoPedido());
        } else {
            System.out.printf("ID Pedido: %d | Fecha: %s | Total: $%.2f | Estado: %s%n",
                    pedido.getIdPedido(),
                    pedido.getFechaPedido().toLocalDate(),
                    pedido.getTotalPedido(),
                    pedido.getEstadoPedido());
        }
    }

    //  MÉTODO NUEVO
    private static void mostrarDetalleFactura(Factura factura, String nombreCliente) {
        System.out.println("\n============== FACTURA DE VENTA ==============");
        System.out.println("Factura N°: " + factura.getIdFactura());
        System.out.println("Fecha de Emisión: " + factura.getFechaFactura().toLocalDate());
        System.out.println("Cliente: " + nombreCliente);
        System.out.println("----------------------------------------------");
        System.out.printf("Total Pedido: $%.2f%n", factura.getTotalFactura());
        System.out.printf("IVA (19%%):    $%.2f%n", factura.getIva());
        System.out.println("==============================================");
    }

    // Nuevo método
    private static void gestionarPedidos(Scanner scanner, IPedidoService pedidoService, IProductoService productoService) {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTIÓN DE PEDIDOS ---");
            System.out.println("1. Actualizar Estado de un Pedido");
            System.out.println("2. Buscar Pedido por ID");
            System.out.println("3. Listar Pedidos por Estado");
            System.out.println("4. Listar Pedidos por Fecha (AAAA-MM-DD)");
            System.out.println("5. Listar Pedidos por ID de Producto");
            System.out.println("6. Listar Todos los Pedidos");
            System.out.println("0. Volver al Menú de Administrador");
            System.out.print("Seleccione una opción: ");

            try {
                int opcion = Integer.parseInt(scanner.nextLine());
                List<Pedido> pedidosEncontrados;

                switch (opcion) {
                    case 1:
                        System.out.print("Ingrese el ID del pedido a actualizar: ");
                        int idPedido = Integer.parseInt(scanner.nextLine());
                        verTodosLosPedidos(pedidoService);
                        System.out.println("Seleccione el nuevo estado:");
                        // Mostrar los estados disponibles
                        for (EstadoPedido estado : EstadoPedido.values()) {
                            System.out.println((estado.ordinal() + 1) + ". " + estado.name());
                        }
                        System.out.print("Opción de estado: ");
                        int opcionEstado = Integer.parseInt(scanner.nextLine());
                        EstadoPedido nuevoEstado = EstadoPedido.values()[opcionEstado - 1];
                        pedidoService.actualizarEstadoPedido(idPedido, nuevoEstado);
                        break;
                    case 2:
                        System.out.print("Ingrese el ID del pedido a buscar: ");
                        int idBuscar = Integer.parseInt(scanner.nextLine());
                        pedidoService.obtenerPedidoPorId(idBuscar)
                                .ifPresentOrElse(
                                        p -> imprimirResumenPedido(p, true),
                                        () -> System.out.println("No se encontró ningún pedido con ese ID.")
                                );
                        break;
                    case 3:
                        System.out.println("Seleccione el estado a buscar:");
                        for (EstadoPedido estado : EstadoPedido.values()) {
                            System.out.println((estado.ordinal() + 1) + ". " + estado.name());
                        }
                        System.out.print("Opción de estado: ");
                        int estadoBuscarOpcion = Integer.parseInt(scanner.nextLine());
                        EstadoPedido estadoBuscar = EstadoPedido.values()[estadoBuscarOpcion - 1];
                        pedidosEncontrados = pedidoService.obtenerPedidosPorEstado(estadoBuscar);
                        pedidosEncontrados.forEach(p -> imprimirResumenPedido(p, true));
                        break;
                    case 4:
                        System.out.print("Ingrese la fecha a buscar (formato AAAA-MM-DD): ");
                        java.time.LocalDate fecha = java.time.LocalDate.parse(scanner.nextLine());
                        pedidosEncontrados = pedidoService.obtenerPedidosPorFecha(fecha);

                        // --- VALIDACIÓN AÑADIDA ---
                        if (pedidosEncontrados.isEmpty()) {
                            System.out.println("\nNo se encontraron pedidos para la fecha seleccionada.");
                        } else {
                            System.out.println("\n--- Pedidos encontrados para la fecha " + fecha + " ---");
                            pedidosEncontrados.forEach(p -> imprimirResumenPedido(p, true));
                        }
                        break;
                    case 5:
                        System.out.println("\n-- Buscando Pedidos por Producto --");
                        // Mostrar la lista de productos para que admin pueda elegir un ID
                        mostrarCatalogoCompleto(productoService); // <<-- LÍNEA AÑADIDA

                        System.out.print("\nIngrese el ID del producto contenido en los pedidos: ");
                        int idProducto = Integer.parseInt(scanner.nextLine());
                        pedidosEncontrados = pedidoService.obtenerPedidosPorProducto(idProducto);

                        // --- VALIDACIÓN AÑADIDA ---
                        if (pedidosEncontrados.isEmpty()) {
                            System.out.println("\nNo se encontraron pedidos que contengan ese producto.");
                        } else {
                            System.out.println("\n--- Pedidos encontrados que contienen el producto ID " + idProducto + " ---");
                            pedidosEncontrados.forEach(p -> imprimirResumenPedido(p, true));
                        }
                        break;
                    case 6:
                        verTodosLosPedidos(pedidoService); // Reutilizamos el método que ya existía
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

    // Nuevo método para gestionar Facturas por administrador

    private static void gestionarFacturas(Scanner scanner, IPedidoService pedidoService, IUsuarioService usuarioService) {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTIÓN DE FACTURAS ---");
            System.out.println("1. Buscar Factura por ID");
            System.out.println("2. Listar Todas las Facturas");
            System.out.println("3. Listar Facturas por Cliente");
            System.out.println("0. Volver al Menú de Administrador");
            System.out.print("Seleccione una opción: ");

            try {
                int opcion = Integer.parseInt(scanner.nextLine());
                switch (opcion) {
                    case 1:
                        System.out.print("Ingrese el ID de la factura a buscar: ");
                        int idFactura = Integer.parseInt(scanner.nextLine());
                        pedidoService.obtenerFacturaPorId(idFactura)
                                .ifPresentOrElse(
                                        factura -> {
                                            Pedido pedido = pedidoService.obtenerPedidoPorId(factura.getPedido().getIdPedido()).orElse(null);
                                            if (pedido != null) {
                                                mostrarDetalleFactura(factura, pedido.getCliente().getNombre());
                                            }
                                        },
                                        () -> System.out.println("No se encontró ninguna factura con ese ID.")
                                );
                        break;
                    case 2:
                        System.out.println("\n-- Listado de Todas las Facturas --");
                        List<Factura> todasLasFacturas = pedidoService.obtenerTodasLasFacturas();
                        if (todasLasFacturas.isEmpty()) {
                            System.out.println("No hay facturas registradas en el sistema.");
                        } else {
                            todasLasFacturas.forEach(factura -> {
                                Pedido pedido = pedidoService.obtenerPedidoPorId(factura.getPedido().getIdPedido()).orElse(null);
                                if (pedido != null) {
                                    System.out.printf("ID Factura: %d | Fecha: %s | Cliente: %s | Total: $%.2f%n",
                                            factura.getIdFactura(), factura.getFechaFactura().toLocalDate(), pedido.getCliente().getNombre(), factura.getTotalFactura());
                                }
                            });
                        }
                        break;
                    case 3:
                        System.out.print("Ingrese el ID del cliente para ver sus facturas: ");
                        int idCliente = Integer.parseInt(scanner.nextLine());
                        Usuario cliente = usuarioService.buscarUsuarioPorId(idCliente)
                                .filter(u -> u instanceof Cliente)
                                .orElseThrow(() -> new IllegalArgumentException("ID no encontrado o no corresponde a un cliente."));

                        System.out.println("\n-- Facturas del Cliente: " + cliente.getNombre() + " --");
                        List<Factura> facturasCliente = pedidoService.obtenerFacturasPorCliente(idCliente);
                        if (facturasCliente.isEmpty()) {
                            System.out.println("Este cliente no tiene facturas registradas.");
                        } else {
                            facturasCliente.forEach(factura -> {
                                System.out.printf("ID Factura: %d | Fecha: %s | Total: $%.2f%n",
                                        factura.getIdFactura(), factura.getFechaFactura().toLocalDate(), factura.getTotalFactura());
                            });
                        }
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
}