---
applyTo: "app/src/main/**/*.kt"
description: Reglas de datos y persistencia para CityAlerta
---

# Reglas de datos

- No mezclar logica de red en la UI.
- Crear servicios separados para APIs externas.
- Usar Room para la persistencia local.
- Considerar modo offline cuando la red no este disponible.
- Mostrar mensajes de error claros y directos.
- Mantener las entidades Usuario, Barrio y Reporte como modelos de dominio claros.
- Evitar dependencias innecesarias en la capa de datos.
