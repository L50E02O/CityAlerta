---
applyTo: "**"
---

# Instrucciones base de CityAlerta

## Contexto del proyecto
- App: CityAlerta.
- Plataforma: Android nativo con Kotlin y Jetpack Compose.
- Arquitectura: MVVM.
- Persistencia local: Room.
- Zona objetivo: Manta, Manabi, Ecuador.
- Documentacion general: README.md.

## Reglas globales
- Todo texto visible para el usuario debe estar en espanol, sin acentos ni emojis.
- El codigo debe usar nombres en ingles para variables, funciones y clases.
- Los comentarios, KDoc y documentacion deben estar en espanol, sin acentos ni emojis.
- Usar solo Jetpack Compose para la UI; no usar XML layouts.
- Mantener tipado estricto y evitar Any.
- Priorizar bajo acoplamiento, alta cohesion y separacion clara de responsabilidades.
- No mezclar la logica de red con la UI.
- Mantener la estructura por capas o por caracteristicas cuando aporte claridad.
- Rutas de UI y ViewModel suelen vivir en app/src/main/java/ec/cityalerta/app/view y app/src/main/java/ec/cityalerta/app/viewmodel.
- No hardcodear datos de barrios o poligonos en la UI; usar el repositorio y la fuente de datos definida en el proyecto.

## Calidad minima esperada
- Escribir tests unitarios para logica de negocio, servicios y validaciones.
- Escribir tests de Compose para componentes y flujos de UI relevantes.
- Mantener complejidad ciclomatica baja y extraer funciones pequenas cuando sea necesario.
- Validar todas las entradas del usuario antes de persistir o enviar datos.
- Usar HTTPS para llamadas de red y proteger credenciales o tokens con almacenamiento seguro.
- Comandos utiles: ./gradlew testDebugUnitTest, ./gradlew connectedAndroidTest, ./gradlew jacocoTestReport, ./gradlew installDebug.

## Reglas especificas del dominio
- Usar BottomNavigationBar con Explorar, Post y Mapa.
- Centrar el mapa en Manta con las coordenadas definidas por el proyecto.
- Usar Material3 Cards para el feed de Explorar.
- Aplicar Lazy Loading en listas y recursos pesados.
- En el flujo de reporte, el acceso a camara debe abrir la camara del sistema directamente, sin pasos intermedios.

## Cambios de datos
- Si se modifica el esquema (por ejemplo, barrio), revisar impactos en repositorios, DTOs y pantallas que consumen esos campos.
- Referencia de modelo de datos: README.md.

## SonarQube y calidad
- Tomar como objetivo una cobertura alta en logica de negocio, servicios y validaciones.
- Mantener la duplicacion baja y evitar code smells graves.
- No introducir vulnerabilidades BLOCKER o HIGH.
- Revisar el Quality Gate antes de considerar un cambio listo para merge.
- Usar el MCP de SonarQube para investigar issues, cobertura y complejidad cuando haga falta.

## Versionado y cambios
- Mantener CHANGELOG en espanol cuando el proyecto lo requiera.
- Los cambios deben seguir el estilo del repositorio y evitar duplicaciones innecesarias.