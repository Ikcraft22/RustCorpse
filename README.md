# RustCuerpo - Sistema de Cadáveres para NeoForge 1.21.1

Un mod avanzado que transforma la desconexión de los jugadores en una oportunidad de juego, creando un cadáver persistente y saqueable con características únicas.

## Características Destacadas

*   **Persistencia Total de Inventario**: Captura automáticamente todo el equipo del jugador al salir, incluyendo la mano secundaria (offhand), armadura completa, inventario principal y la barra de acceso rápido (hotbar).
*   **Compatibilidad Multi-Mod**: Integración nativa con **Curios API** y **Accessories API**. El cuerpo guardará automáticamente mochilas, gafas, anillos y cualquier accesorio equipado en slots especiales.
*   **Sistema de Saqueo de Logros**: Los jugadores pueden reclamar los logros y estadísticas del cuerpo mediante la interfaz, permitiendo una progresión de estilo "saqueador".
*   **Interfaz Visual Avanzada**:
    *   **Modelo 3D en Tiempo Real**: La pantalla de saqueo muestra una vista previa del cuerpo utilizando la **skin real del jugador** que se desconectó.
    *   **Renderizado Fiel**: Soporte para capas de skin y modelos Slim/Classic.
    *   **Botón "Take All" Inteligente**: Vacía el cuerpo con un solo clic, transfiriendo ítems al inventario del saqueador o soltándolos en el suelo si no hay espacio.
*   **Renderizado en el Mundo**: 
    *   Los cuerpos aparecen físicamente en el mundo, acostados en la posición de desconexión, manteniendo la identidad visual del jugador.
    *   **Sistema Anti-Solapamiento**: Algoritmo de micro-desplazamiento aleatorio para evitar el parpadeo de texturas (Z-fighting) cuando varios jugadores desconectan en el mismo punto.
*   **Optimización de Mundo (Auto-Clean)**:
    *   El cuerpo se desintegra automáticamente cuando el inventario queda vacío, evitando la acumulación de entidades innecesarias.
    *   Lógica de limpieza inmediata tras usar el botón "Take All".
*   **Capacidades NeoForge**: Implementación moderna utilizando el sistema de *Capabilities* de NeoForge para una gestión de inventario robusta y compatible.

## Requisitos

*   **Minecraft**: 1.21.1
*   **NeoForge**: 21.1.219+
*   **(Opcional)**: Curios API o Accessories API para soporte de slots adicionales.

---
*Desarrollado con enfoque en la calidad de código y la experiencia de usuario en servidores survival/PvP.*
