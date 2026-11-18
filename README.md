# Humanizador AI

Aplicación Android nativa en Kotlin + Jetpack Compose y servidor Node.js.

## Funciones incluidas
- Editor de texto.
- Importar y exportar documentos Word (.docx).
- Estilos: natural/conversacional, académico, profesional y sencillo.
- Perfil de escritura personal opcional.
- Backend con OpenAI Responses API.

## Backend
1. Despliega la carpeta `server/` en un hosting compatible con Node.js.
2. Configura `OPENAI_API_KEY` como variable secreta en el hosting.
3. Configura `OPENAI_MODEL` con un modelo disponible para tu cuenta.
4. Ejecuta `npm install` y `npm start`.
5. Comprueba la ruta `/health`.
6. En la app, abre Ajustes y pega la URL HTTPS terminada en `/humanize`.

Contrato: POST JSON con `text`, `style` y `writing_profile`; respuesta JSON `{"result":"..."}`.

**Seguridad:** nunca subas una clave real al repositorio ni la pongas dentro de la app Android. Guárdala solo en variables secretas del servidor. La API puede generar cargos según uso y modelo.

El editor busca mejorar claridad, coherencia y naturalidad. No garantiza ni intenta evadir detectores de IA.

## Android
Abre el proyecto raíz en Android Studio, sincroniza Gradle y ejecuta la app en un dispositivo o emulador. Para generar APK: Build > Build Bundle(s) / APK(s) > Build APK(s).
