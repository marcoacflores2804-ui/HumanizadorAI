import "dotenv/config";
import express from "express";
import cors from "cors";
import helmet from "helmet";
import rateLimit from "express-rate-limit";
import OpenAI from "openai";

const app = express();
const port = Number(process.env.PORT || 3000);
const model = process.env.OPENAI_MODEL || "gpt-6-luna";

app.disable("x-powered-by");
app.use(helmet());
app.use(cors({
  origin: process.env.CORS_ORIGIN
    ? process.env.CORS_ORIGIN.split(",").map((value) => value.trim())
    : "*"
}));
app.use(express.json({ limit: "200kb" }));

const rewriteLimiter = rateLimit({
  windowMs: 60 * 1000,
  limit: 10,
  standardHeaders: "draft-7",
  legacyHeaders: false,
  message: { error: "Has realizado demasiadas solicitudes. Intenta de nuevo en un minuto." }
});

const client = process.env.OPENAI_API_KEY
  ? new OpenAI({ apiKey: process.env.OPENAI_API_KEY })
  : null;

app.get("/", (_req, res) => {
  res.json({
    name: "HumanizadorAI API",
    status: "online",
    version: "0.1.0",
    endpoints: { health: "/health", rewrite: "POST /api/rewrite" }
  });
});

app.get("/health", (_req, res) => {
  res.json({ status: "ok", aiConfigured: Boolean(client), model });
});

const styles = new Set(["natural", "academic", "professional", "simple", "friendly"]);
const tones = new Set(["neutral", "formal", "casual", "persuasive", "clear"]);

app.post("/api/rewrite", rewriteLimiter, async (req, res) => {
  try {
    if (!client) {
      return res.status(503).json({
        error: "El motor de IA todavía no está configurado en el servidor."
      });
    }

    const { text, style = "natural", tone = "neutral", preserveLength = true } = req.body ?? {};

    if (typeof text !== "string" || !text.trim()) {
      return res.status(400).json({ error: "Escribe o pega un texto para comenzar." });
    }
    if (text.length > 12000) {
      return res.status(413).json({ error: "Por ahora el límite es de 12,000 caracteres por solicitud." });
    }

    const selectedStyle = styles.has(style) ? style : "natural";
    const selectedTone = tones.has(tone) ? tone : "neutral";

    const instructions = [
      "Eres el asistente de edición de HumanizadorAI.",
      "Reescribe el texto para mejorar claridad, fluidez, naturalidad y corrección.",
      "Conserva fielmente las ideas, datos, nombres, citas y la intención del original.",
      "No inventes fuentes, experiencias personales, hechos ni referencias.",
      "No afirmes que el texto fue escrito por una persona ni prometas evadir detectores de IA.",
      "Respeta el idioma original. Devuelve únicamente el texto revisado, sin introducciones ni explicaciones.",
      `Estilo: ${selectedStyle}.`,
      `Tono: ${selectedTone}.`,
      preserveLength === false
        ? "Puedes reorganizar y ajustar la extensión cuando ayude a la claridad."
        : "Mantén una extensión similar a la del texto original."
    ].join("\n");

    const response = await client.responses.create({
      model,
      instructions,
      input: text,
      store: false
    });

    const rewrittenText = response.output_text?.trim();
    if (!rewrittenText) {
      return res.status(502).json({ error: "La IA no devolvió texto. Intenta nuevamente." });
    }

    return res.json({
      result: rewrittenText,
      style: selectedStyle,
      tone: selectedTone,
      model
    });
  } catch (error) {
    console.error("Rewrite error:", error?.message || error);
    return res.status(500).json({
      error: "No se pudo procesar el texto. Revisa la configuración del servidor e inténtalo de nuevo."
    });
  }
});

app.listen(port, "0.0.0.0", () => {
  console.log(`HumanizadorAI API escuchando en el puerto ${port}`);
});
