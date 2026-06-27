<!-- xxxxxxxxxxxxxxxxxxx

/*
* 🛠️ Proyecto: Servitec (proserv)
Sistema de Gestión de Servicios Técnicos

* Login usuarios (administrador, técnico, cliente) prototipo app
* user: admin1@gmail.com, pass: 123456
* user: tecnico1@gmail.com, pass: 123456
* user: tecnico2@gmail.com, pass: 123456
* user: cliente1@gmail.com, pass: 123456
* user: cliente2@gmail.com, pass: 123456
* user: cliente3@gmail.com, pass: 123456
* *
* */
1. Introducción y Propósito
Servitec es una aplicación móvil diseñada para optimizar el flujo de trabajo de empresas de soporte técnico. Permite el registro de equipos (activos), la apertura de órdenes de servicio, el seguimiento en tiempo real por parte de técnicos y la gestión de clientes, todo centralizado en una base de datos en la nube.
2. Stack Tecnológico y Dependencias
La aplicación está construida bajo estándares modernos de desarrollo nativo en Android:
•
Lenguaje: Kotlin (1.9+)
•
Interfaz de Usuario: Jetpack Compose (Material 3) para una UI declarativa y moderna.
•
Arquitectura: MVVM (Model-View-ViewModel) con Repositorios.
•
Backend como Servicio (BaaS): Supabase, utilizado para:
◦
Postgrest: Operaciones CRUD sobre la base de datos PostgreSQL.
◦
Auth: Gestión de sesiones y seguridad de usuarios.


 xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-->