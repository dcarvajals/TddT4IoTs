# Arquitectura del Sistema TDDT4IOTS

Este repositorio contiene la implementación del sistema TDDT4IOTS. A continuación, se detalla la arquitectura del sistema, que está dividida en varias capas y componentes, cada uno con su propósito específico.

![Arquitectura del Sistema](./Arquitecturas/Arquitectura%20TDD%20-%20Redusida.png)

## Capas de la Arquitectura

### Capa de Front-End
- **UML Modeling**: Utiliza las herramientas JsUml2 y Armadillo para la modelización UML.
- **IoT Device**: Utiliza GoJS para la representación de dispositivos IoT.
- **Aplicación TDDT4IOTS**: Desarrollada con AngularJs, Bootstrap, Jquery, Store2, Sweetalert2 y Toastr para la interacción del usuario y la planificación.
- **WebSocket Client**: Gestiona la comunicación en tiempo real con el servidor a través de eventos como `OnOpen()`, `OnMessage()`, y `OnClose()`.

### Capa de Middleware
- **Apache Tomcat 9.0.46**: Servidor de aplicaciones que maneja las solicitudes REST.
- **Json Web Token (JWT)**: Utilizado para la validación de tokens y asegurar las comunicaciones.
- **Interfaz de Programación de Aplicaciones (APIs REST)**: Expone los servicios necesarios para la interacción con otras capas y componentes.

### Capa de Negocios
- **Aplicación Java (Jakarta EE)**: Desarrollada con Oracle Open JDK 11.0.15, maneja la lógica de negocios de la aplicación.
- **Comandos CMD**: Incluye herramientas como Angular CLI 15.1.6 y Spring CLI 2.7.3 para el manejo y despliegue de componentes.
- **WebSocket Server**: Servidor que gestiona las comunicaciones WebSocket para la interacción en tiempo real.

### Capa de Datos
- **Base de Datos PostgreSQL**: Almacena datos de usuarios, proyectos y componentes.
- **Archivos JSON**: Almacenan diagramas de clases, diagramas de casos de uso, componentes IoT y otros diagramas relevantes.

### Capa de Dispositivos IoT
- **IoT Daemon**: Gestiona la conexión y comunicación con la placa Arduino, permitiendo la descarga y ejecución del código generado por TDDT4IOTS en la placa.

## Descripción de Flujo

1. **Interacción del Usuario**: Los usuarios interactúan con la aplicación TDDT4IOTS a través de la interfaz web.
2. **Proceso de Login**: Los usuarios se autentican mediante un proceso de login que utiliza JWT para la validación de tokens.
3. **Comunicación REST**: Las solicitudes de la aplicación se envían al servidor Apache Tomcat, donde se procesan y se comunican con la capa de negocios.
4. **Lógica de Negocios**: La aplicación Java maneja la lógica de negocios y realiza operaciones en la base de datos PostgreSQL.
5. **Actualización y Consulta de Datos**: La capa de negocios interactúa con los archivos JSON y la base de datos para almacenar y recuperar datos necesarios.
6. **Interacción en Tiempo Real**: Las comunicaciones en tiempo real se manejan a través de WebSocket, facilitando la interacción continua y fluida entre el cliente y el servidor.
7. **Gestión de Dispositivos IoT**: El IoT Daemon se encarga de gestionar la comunicación con la placa Arduino, permitiendo la descarga y ejecución del código necesario.

## Tecnologías Utilizadas

- **Frontend**: AngularJs, Bootstrap, Jquery, Store2, Sweetalert2, Toastr, JsUml2, Armadillo, GoJS.
- **Middleware**: Apache Tomcat, JWT.
- **Backend**: Java (Jakarta EE), Oracle Open JDK, Spring CLI, Angular CLI.
- **Database**: PostgreSQL.
- **Real-time Communication**: WebSocket.
- **IoT**: Arduino, IoT Daemon.

## Contribución

Si deseas contribuir a este proyecto, por favor sigue los siguientes pasos:
1. Realiza un fork del repositorio.
2. Crea una nueva rama (`git checkout -b feature/nueva-funcionalidad`).
3. Realiza los cambios necesarios y commitea (`git commit -m 'Añadir nueva funcionalidad'`).
4. Sube los cambios a tu repositorio (`git push origin feature/nueva-funcionalidad`).
5. Crea un Pull Request hacia la rama principal de este repositorio.

## Licencia

Este proyecto está bajo la licencia [MIT](LICENSE).
