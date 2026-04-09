# ManTap

Aplicación móvil nativa para Android que permite a los usuarios de Manta, Manabí, Ecuador reportar y explorar información sobre zonas de la ciudad.

## Descripción

ManTap es una solución móvil diseñada exclusivamente para facilitar la comunicación comunitaria mediante reportes de zonas. La aplicación permite a los usuarios explorar información geográfica, crear reportes ubicados en el mapa y participar en la mejora continua de su comunidad.

## Especificaciones del Proyecto

**Ubicación:** Manta, Manabí, Ecuador  
**Plataforma:** Android Nativo  
**Lenguaje:** Kotlin  
**Framework de UI:** Jetpack Compose  
**Arquitectura:** MVVM (Model-View-ViewModel)  

## Stack Tecnológico

### Core
- **Kotlin** - Lenguaje de programación principal
- **Jetpack Compose** - Framework de UI declarativo (prohibido usar XML layouts)
- **Android Architecture Components** - ViewModel, LiveData, StateFlow

### Persistencia
- **Room Database** - Base de datos local
  - Entidad: Usuario (PK: cedula)
  - Entidad: Barrio
  - Entidad: Reporte

### Mapas y Geolocalización
- **Google Maps** - Visualización de zonas
- **Ubicación base:** Lat: -0.967653, Lng: -80.708910

### Testing
- **JUnit** - Tests unitarios
- **Compose Testing Library** - Tests de componentes UI

## Arquitectura

La aplicación sigue el patrón **MVVM** con separación clara de capas:

```
├── ui/
│   ├── screens/          # Pantallas principales
│   ├── components/       # Componentes reutilizables Compose
│   └── navigation/       # Lógica de navegación
├── viewmodel/            # ViewModels (lógica de presentación)
├── data/
│   ├── local/            # Room database
│   ├── remote/           # APIs externas
│   └── repository/       # Abstracción de datos
├── domain/               # Entidades y casos de uso
└── utils/                # Utilidades comunes
```

## Funcionalidades Principales

### 1. Navegación Principal
La aplicación implementa un BottomNavigationBar con tres secciones:

- **Explorar** (Icono: Compass) - Visualizar reportes en feed
- **Post** (Icono: Plus) - Crear reportes (solo usuarios autenticados)
- **Mapa** (Icono: Map) - Vista geográfica centrada en Manta

### 2. Autenticación
- Soporte para usuario autenticado y modo invitado
- Validación obligatoria de cédula ecuatoriana (10 dígitos)
- Persistencia de sesión con Room

### 3. Modo Invitado
- Los usuarios invitados pueden explorar y ver el mapa
- **Restricción:** No pueden acceder a la funcionalidad de reportes ([+])

### 4. Reportes
- Crear reportes con ubicación geográfica
- Asociar reportes a barrios específicos
- Persistencia y sincronización de datos

## Paleta de Colores

| Elemento | Color | Valor |
|----------|-------|-------|
| Primario | Ocean Blue | #0077B6 |
| Peligro | Danger Red | - |
| Advertencia | Warning Yellow | - |
| Seguridad | Safety Green | - |

## Validaciones Requeridas

### Validación de Cédula Ecuatoriana
La aplicación implementa obligatoriamente el algoritmo de validación para cédulas de 10 dígitos ecuatorianas durante el registro de usuarios.

## Guías de Desarrollo

### Nomenclatura de Código

**PascalCase:**
- Componentes de Compose
- Pantallas (Screens)
- Clases principales

**camelCase:**
- Funciones
- Variables
- Estados

### Lenguaje

| Contexto | Idioma| Ejemplo |
|----------|-------|---------|
| UI (visible usuario) | Español | "Iniciar Sesión", "Reportar zona" |
| Código (variables/funciones) | Inglés | `userReport`, `idCard`, `fetchData` |
| Comentarios | Español (sin acentos) | `// Valida informacion de usuario` |
| Commits | Español (sin acentos) | `feat: agregar validacion cedula` |


### Recomendaciones de Rendimiento

- Implementar Lazy Loading en listas
- Evitar recomposiciones innecesarias en Compose
- Considerar funcionalidad offline
- Mensajes de error claros para fallos de conectividad

## Testing

### Tests Unitarios (Obligatorio)
- Lógica de negocio
- Servicios
- Validaciones (especialmente validación de cédula)

### Tests de Componentes
- Utilizar librería estándar de testing para Jetpack Compose
- Validar interacciones de UI

## Estructura de Datos

### Entidad: Usuario
```
cedula (String) - PK
nombre (String)
email (String)
esInvitado (Boolean)
```

### Entidad: Barrio
```
id (Int) - PK
nombre (String)
ciudad (String)
```

### Entidad: Reporte
```
id (Int) - PK
cedulaUsuario (String) - FK
barrioId (Int) - FK
descripcion (String)
latitud (Double)
longitud (Double)
fecha (Long)
```

## Instalación

### Prerequisitos
- Android Studio Flamingo o superior
- JDK 11 o superior
- SDK de Android 31+

### Pasos

1. Clonar el repositorio
```bash
git clone <repositorio>
cd ManTap
```

2. Obtener dependencias
```bash
./gradlew build
```

3. Configurar Google Maps API Key
   - Agregar key en `local.properties` o AndroidManifest.xml

4. Ejecutar la aplicación
```bash
./gradlew installDebug
```

## Contribuciones

### Estándares de Commit
- Formato: `tipo: descripcion`
- Tipos: `feat`, `fix`, `docs`, `style`, `refactor`, `test`
- Lenguaje: Español sin acentos
- Ejemplo: `feat: agregar pantalla de reportes`

### Pull Requests
- Incluir tests unitarios para nuevas funcionalidades
- Seguir nomenclatura establecida
- Documentar cambios significativos

## Roadmap

- [ ] Autenticación con servidor backend
- [ ] Sincronización en tiempo real
- [ ] Sistema de comentarios en reportes
- [ ] Notificaciones push
- [ ] Filtrado avanzado de reportes por zona

## Contacto y Soporte

Para dudas o reporte de errores, contactar al equipo de desarrollo.

---

**Última actualización:** Abril 2026
