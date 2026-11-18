import "dotenv/config";
import express from "express";
import rateLimit from "express-rate-limit";
import OpenAI from "openai";

const app = express();
app.use(express.json({ limit: "1mb" }));

const apiKey = process.env.OPENAI_API_KEY;
const model = process.env.OPENAI_MODEL;
if (!model) {
  console.error("Falta OPENAI_MODEL. Configura un identificador de modelo habilitado para tu cuenta.");
  process.exit(1);
}

if (!apiKey) {
  console.error("Falta OPENAI_API_KEY en las variables de entorno.");
  process.exit(1);
}

const openai = new OpenAI({ apiKey });

app.get("/health", (_req, res) => res.json({ ok: true, service: "Humanizador AI" }));

app.use("/humanize", rateLimit({
  windowMs: 60 * 1000,
  limit: 12,
  standardHeaders: "draft-7",
  legacyHeaders: false
}));

app.post("/humanize", async (req, res) => {
  try {
    const { text, style = "Natural y conversacional", writing_profile = "" } = req.body ?? {};
    if (typeof text !== "string" || !text.trim()) {
      return res.status(400).json({ error: "Escribe o importa un texto." });
    }
    if (text.length > 30000) {
      return res.status(413).json({ error: "El texto supera el límite de 30,000 caracteres." });
    }

    const safeStyle = String(style).slice(0, 80);
    const profile = typeof writing_profile === "string" ? writing_profile.slice(0, 5000) : "";

    const instructions = `
Eres un editor profesional de español. Revisa y reescribe el texto para mejorar claridad,
coherencia, ritmo, precisión y naturalidad, sin cambiar su significado.
Estilo solicitado: ${safeStyle}.
Si se proporciona una muestra de escritura personal, úsala como referencia general de tono,
nivel de formalidad, vocabulario y ritmo. No copies frases completas de la muestra.
Muestra de referencia:
${profile || "(No se proporcionó muestra personal)."}
Conserva hechos, cifras, nombres, citas, fuentes y referencias.
No inventes experiencias personales, datos ni fuentes.
Mantén el idioma original salvo petición expresa.
Evita redundancias y frases rígidas.
No añadas errores deliberados ni afirmes que el texto fue escrito por una persona.
No prometas ni optimices para evadir detectores de IA.
Devuelve únicamente el texto revisado, sin introducciones ni explicaciones.
`.trim();

    const response = await openai.responses.create({ model, instructions, input: text, store: false });
    const result = response.output_text?.trim();
    if (!result) return res.status(502).json({ error: "La IA no devolvió texto. Inténtalo de nuevo." });
    return res.json({ result });
  } catch (error) {
    console.error("Error en /humanize:", error?.message || error);
    return res.status(500).json({ error: "No se pudo procesar el texto. Revisa la conexión e inténtalo de nuevo." });
  }
});

const port = Number(process.env.PORT || 3000);
app.listen(port, "0.0.0.0", () => console.log(`Humanizador AI backend escuchando en el puerto ${port}`));
