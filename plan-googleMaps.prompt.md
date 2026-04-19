## Plan: Integrar Google Maps con Límites de Polígono GeoJSON (Datos en Memoria)

**Objetivo:** Agregar una pantalla de Google Maps que muestre una ciudad (Manta) con límites de polígono GeoJSON. El usuario solo puede seleccionar puntos dentro del área definida. Los datos de prueba existen solo en memoria durante la ejecución de la app. Aplicar Factory Pattern y arquitectura MVVM.

### Steps

1. **Agregar dependencias de Google Maps** en `libs.versions.toml` y `build.gradle.kts`
   - Play Services Maps (para Google Maps)
   - Maps Compose library (para integración con Jetpack Compose)

2. **Crear modelos de datos de Ciudad y GeoJSON** en `model/data/CiudadModels.kt`
   - Data class `Ciudad(id, nombre, pais, geojson, centro_lat, centro_lng)`
   - Data class `GeoJson(type, features)`
   - Data class `Features(type, properties, geometry)`
   - Data class `Geometry(coordinates)` - lista anidada de coordenadas
   - Data class `Properties(name, country)`
   - Data class `MapMarker(id, latitude, longitude, title, description)`

3. **Crear utilidades de conversión GeoJSON** en `model/utils/GeoJsonConverter.kt`
   - Función `geoJsonPolygonToGoogleMapsPolygon()`: convierte GeoJSON polygon a PolygonOptions
   - Función `invertLatLng()`: invierte latitud/longitud de GeoJSON a Google Maps
   - Función `pointInPolygon()`: valida si un punto está dentro del polígono (algoritmo ray casting)

4. **Crear datos de prueba (Mock Data)** en `model/data/MockData.kt`
   - Función `getMantaCity()`: retorna un `Ciudad` hardcodeado
   - Ciudad: Manta, Ecuador
   - GeoJSON parseado del archivo `manta.geojson` incluido en el proyecto
   - Coordenadas centro: `centro_lat = -0.95`, `centro_lng = -80.73`
   - Data solo existe en memoria durante la ejecución

5. **Crear capa de datos: MapRepository** en `model/repository/MapRepository.kt`
   - Interfaz `IMapRepository` con métodos para ciudades y marcadores
   - Clase `MapRepository` que retorna datos desde `MockData`
   - Método `getCiudadById(id)`: retorna la ciudad de prueba (Manta)
   - Método `addMarker()`, `removeMarker()` para gestionar marcadores en memoria

6. **Crear ViewModel: MapViewModel** en `viewmodel/MapViewModel.kt`
   - Data class `MapUiState(ciudad, marcadores, cameraZoom, errorMessage, isLoading, isPointValid)`
   - Clase `MapViewModel` que carga ciudad desde repository
   - Método `onMapClicked(latLng)`: valida si punto está dentro del polígono, agrega marcador si es válido
   - Método `loadCiudad(ciudadId)`: carga datos de la ciudad y GeoJSON
   - Método `removeMarker(markerId)`: elimina marcador de la lista

7. **Extender ViewModelFactory** en `viewmodel/ViewModelFactory.kt`
   - Cambiar nombre de `AuthViewModelFactory` a `AppViewModelFactory`
   - Agregar constructor con `context` para instanciar `MapRepository`
   - Agregar lógica para crear `MapViewModel` con `MapRepository` inyectado
   - Mantener instanciación de `AuthViewModel`

8. **Crear composable MapScreen** en `view/map/MapScreen.kt`
   - Recibir `ciudadId` como parámetro de ruta
   - Mostrar mapa centrado en `centro_lat` y `centro_lng` de la ciudad
   - Renderizar polígono GeoJSON en color semi-transparente (azul con opacidad)
   - Renderizar marcadores agregados por el usuario
   - Manejar clics del mapa: 
     - Si punto está dentro: agregar marcador
     - Si punto está fuera: mostrar Toast indicando que debe estar dentro del polígono
   - Mostrar loading mientras se cargan datos
   - Mostrar botón para eliminar marcadores

9. **Actualizar rutas de navegación** en `navigation/routes.kt`
   - Verificar/crear ruta `Map` que acepte parámetro `ciudadId` (ej: `map/{ciudadId}`)

10. **Actualizar AppNavigation** en `navigation/navigation.kt`
    - Pasar `Context` a `AppNavigation` desde `MainActivity`
    - Instanciar `MapViewModel` usando factory actualizado
    - Agregar ruta composable para `MapScreen` con parámetro dinámico `ciudadId`

11. **Actualizar HomeScreen** en `view/home/homeScreen.kt`
    - Agregar botón "Ver Mapa" o "Prueba de Mapa"
    - Navegar a `Map` con `ciudadId = "manta"`
    - Recibir `NavController` como parámetro

12. **Configurar API Key en AndroidManifest.xml**
    - Declarar meta-data con Google Maps API Key bajo `<application>`
    - Agregar permisos: `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `INTERNET`

13. **Actualizar MainActivity.kt**
    - Pasar `this` (contexto) a `AppNavigation()`

### Further Considerations

1. **Futuro: Integración con Supabase:** Cuando se implemente, reemplazar `MockData` con llamadas a Supabase en `MapRepository`.

2. **Futuro: Múltiples ciudades:** Extender `MockData` o reemplazarla con búsqueda desde Supabase.

3. **Tipos de geometría GeoJSON:** Plan actual solo soporta Polygon. Se puede extender para MultiPolygon si es necesario.

4. **Guardado de marcadores:** Actualmente solo existen en memoria. Se pueden guardar en Supabase o Room Database en el futuro.

5. **Permisos en runtime:** Si se agrega geolocalización real, considerar solicitar permisos en tiempo de ejecución (Android 6+).
