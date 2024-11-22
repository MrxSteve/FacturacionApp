# FacturacionApp

## Descripción
FacturacionApp es una aplicación Spring Boot diseñada para gestionar facturas. 
Permite a los usuarios crear, ver y eliminar facturas, convertirlas a PDF, así como enviar por correo electrónico. 
La aplicación utiliza Thymeleaf para el front-end e integra una base de datos MySQL para la persistencia de datos.

## Características
- Crear, ver y eliminar facturas
- Generar archivos PDF de facturas
- Crear, ver, modificar y eliminar clientes
- Crear, ver, modificar y eliminar productos
- Enviar facturas por correo electrónico con archivos PDF adjuntos
- Autenticación y autorización de usuarios con Spring Security
- Validación de formularios y manejo de errores
- Búsqueda de productos con autocompletado

## Tecnologías
- Java 17
- Spring Boot 3.3.5
- Spring Data JPA
- Spring Security
- Thymeleaf
- MySQL
- Maven
- Base de datos H2 (para pruebas)
- OpenPDF (para generación de PDF)
- Spring Boot Starter Mail (para funcionalidad de correo electrónico)

## Requisitos previos
- Java 17 o superior
- Maven
- MySQL

## Configuración
1. Clona el repositorio:
    ```sh
    git clone https://github.com/MrxSteve/FacturacionApp.git
    cd FacturacionApp
    ```

2. Configura la base de datos:
    - Actualiza el archivo `application.properties` con tus credenciales de MySQL.

3. Construye el proyecto:
    ```sh
    mvn clean install
    ```

4. Ejecuta la aplicación:
    ```sh
    mvn spring-boot:run
    ```

## Uso
- Accede a la aplicación en `http://localhost:8080`.
- Usa las siguientes credenciales para iniciar sesión:
    - Usuario: `user`, Contraseña: `user`
    - Administrador: `admin`, Contraseña: `admin`

## Endpoints
- `/` ó `/listar` : Página de inicio
- `/factura/form/{clienteId}`: Formulario para crear factura
- `/factura/ver/{id}`: Ver factura
- `/ver/{clienteId}`: Ver facturas de un cliente
- `/factura/eliminar/{id}`: Eliminar una factura
- `/productos/form`: Formulario para crear producto
- `/productos/ver/{id}`: Ver producto

## Licencia
Este proyecto está licenciado bajo la Licencia MIT.