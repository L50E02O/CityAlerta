---
applyTo: "**"
---

# Instrucciones para GitHub Copilot: Aplicaciones Móviles Nativas

## Contexto y Alcance
Nombre de la App: ManTap.

Ubicación Objetivo: Manta, Manabí, Ecuador.

Tecnología: Android Nativo con Kotlin y Jetpack Compose.

Arquitectura: MVVM (Model-View-ViewModel).

Persistencia: Room (Entidades: Usuario [PK: cedula], Barrio, Reporte).

## Reglas de Salida y Lenguaje
Interfaz de Usuario (UI): Todo el texto visible para el usuario en español (ej. "Iniciar Sesion", "Reportar zona").

Código (Variables/Funciones/Clases): En inglés (ej. userReport, idCard, fetchData).

Comentarios y Documentación: En español, sin emojis y sin acentos.

Commits: Formato tipo: descripcion (feat, fix, docs, style, refactor, test), en español y sin acentos.

lenguaje: Todo lo generado debe estar en español, sin acentos ni emojis, tanto en la UI como en los comentarios y mensajes de commit. El código (nombres de variables, funciones, clases) debe estar en inglés para mantener consistencia con las convenciones de programación.
## Estructura y Estándares de Código
Evitar el uso de XML layouts; usar exclusivamente Jetpack Compose para la UI.

Nunca usar emogis en el código, comentarios, documentación o mensajes de commit.

UI Framework: Uso exclusivo de Jetpack Compose. Prohibido el uso de XML layouts.

Organización: Estructura por capas o características: pantallas (screens), componentes, navegación, servicios, utilidades y tipos/modelos.

Nomenclatura:

PascalCase: Para componentes de Compose y pantallas.

camelCase: Para funciones, variables y estados.

Tipado: Tipado estricto de Kotlin; evitar el uso de Any.

Rendimiento: Implementar Lazy Loading en listas y recursos pesados; evitar re-renders (recomposiciones) innecesarios.

## Requerimientos Específicos de ManTap
Validación de Cédula: Implementar obligatoriamente el algoritmo de validación de 10 dígitos para cédulas ecuatorianas en el registro.

Lógica de Navegación:

Implementar un BottomNavigationBar con:

Explorar (Icono: Compass)

Post (Icono: Add/Plus)

Mapa (Icono: Map)

Modo Invitado: Bloquear el acceso a la pantalla de reporte ([+]) si userSession.isGuest es true.

Capa de Datos: Servicios para APIs externos y Room para persistencia; no mezclar lógica de red en la UI. Considerar modo offline y mensajes de error claros.

## Guías de Estilo Visual (UI)
Paleta de Colores: * Primario: Ocean Blue (#0077B6).

Semánticos: Danger Red, Warning Yellow, Safety Green.

Componentes: Uso de Material3 Cards para el feed de la pantalla "Explorar".

Mapa: Integración con Google Maps centrado en las coordenadas de Manta:

Lat: -0.967653, Lng: -80.708910

## Testing
Tests Unitarios: Obligatorios para lógica de negocio, servicios y validaciones.

Tests de Componentes: Utilizar la librería estándar de testing para Jetpack Compose.