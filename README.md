# Humanizador AI

Aplicación Android nativa en Kotlin + Jetpack Compose y servidor Node.js.

## Funciones
- Editor de texto.
- Importar y exportar documentos Word (.docx).
- Estilos: natural/conversacional, académico, profesional y sencillo.
- Perfil de escritura personal opcional.
- Backend con OpenAI Responses API.

## Desplegar el servidor en Render
El archivo `render.yaml` define un servicio web para la carpeta `server/`.

1. En Render, crea un **New + → Blueprint** y conecta este repositorio.
2. Render leerá `render.yaml` y creará el servicio.
3. En Environment, agrega `OPENAI_API_KEY` como variable secreta.
4. El modelo configurado inicialmente es `gpt-6-luna`; confirma que esté habilitado para tu proyecto API.
5. Espera a que el deploy termine y comprueba la URL pública en `/health`.
6. Copia la URL base HTTPS y agrégale `/humanize`.
7. En la app Android, abre Ajustes y pega esa URL.

## API
POST JSON: `{"text":"texto","style":"Natural y conversacional","writing_profile":"muestra opcional"}`
Respuesta: `{"result":"texto revisado"}`

**Seguridad:** nunca subas una clave real al repositorio ni la pongas dentro de la app Android. Guárdala solo como variable secreta del servidor. Las llamadas a la API pueden generar cargos según el modelo y consumo.

La herramienta mejora claridad, coherencia y naturalidad; no garantiza ni intenta evadir detectores de IA.

## Android
Abre el proyecto raíz en Android Studio, sincroniza Gradle y ejecuta la app en un dispositivo o emulador. Para generar APK: Build > Build Bundle(s) / APK(s) > Build APK(s).
