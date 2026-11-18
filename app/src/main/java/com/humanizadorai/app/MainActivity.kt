package com.humanizadorai.app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument

private val Ink = Color(0xFF111329)
private val Purple = Color(0xFF8067F5)
private val Muted = Color(0xFF9DA2B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HumanizadorApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HumanizadorApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var style by remember { mutableStateOf("Natural y conversacional") }
    var writingProfile by remember { mutableStateOf("") }
    var showProfile by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }

    val openDocx = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            try {
                input = withContext(Dispatchers.IO) { readDocx(context, uri) }
                Toast.makeText(context, "Documento cargado", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo leer el DOCX: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    val saveDocx = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    ) { uri ->
        if (uri != null) scope.launch {
            try {
                withContext(Dispatchers.IO) { writeDocx(context, uri, output.ifBlank { input }) }
                Toast.makeText(context, "Documento Word guardado", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo guardar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Purple, background = Ink, surface = Color(0xFF1B1E38),
        onBackground = Color.White, onSurface = Color.White
    )) {
        Column(
            Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF191A39), Ink)))
                .verticalScroll(rememberScrollState()).padding(20.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("HUMANIZADOR AI", color = Color(0xFFB7A9FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Escribe con naturalidad.", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    Text("Editor local de escritura", color = Muted, fontSize = 14.sp)
                }
                Text("MODO SIN INTERNET", color = Color(0xFF9BE7C4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(22.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF202342)), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tu texto", fontWeight = FontWeight.Bold)
                        TextButton(onClick = { openDocx.launch(arrayOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) }) {
                            Text("＋ Abrir Word")
                        }
                    }
                    OutlinedTextField(
                        value = input, onValueChange = { input = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
                        placeholder = { Text("Pega aquí tu texto o importa un documento .docx…", color = Muted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple, unfocusedBorderColor = Color(0xFF454966),
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Purple
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("ESTILO", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    ExposedDropdownMenuBox(expanded = menu, onExpandedChange = { menu = !menu }) {
                        OutlinedTextField(
                            value = style, onValueChange = {}, readOnly = true,
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menu) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple, unfocusedBorderColor = Color(0xFF454966))
                        )
                        ExposedDropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                            listOf("Natural y conversacional", "Académico", "Profesional", "Sencillo y claro").forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = { style = it; menu = false })
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { showProfile = !showProfile }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)) {
                        Text(if (showProfile) "Ocultar perfil de escritura" else "✎ Configurar mi perfil de escritura")
                    }
                    if (showProfile) {
                        Text("MUESTRA DE TU FORMA DE ESCRIBIR", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = writingProfile, onValueChange = { writingProfile = it },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 110.dp),
                            placeholder = { Text("Pega un texto escrito por ti para orientar tono, vocabulario y ritmo.", color = Muted) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Purple,
                                unfocusedBorderColor = Color(0xFF454966), focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Text("Usa una muestra propia y evita incluir datos privados.", color = Muted, fontSize = 11.sp)
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (input.isBlank()) {
                                Toast.makeText(context, "Primero escribe o importa un texto", Toast.LENGTH_SHORT).show()
                            } else {
                                busy = true
                                scope.launch {
                                    try {
                                        output = withContext(Dispatchers.Default) {
                                            LocalTextEngine.rewrite(input, style, writingProfile)
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No se pudo revisar el texto: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally { busy = false }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple),
                        shape = RoundedCornerShape(14.dp), enabled = !busy
                    ) {
                        if (busy) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("✦ Mejorar texto sin internet", fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF202342)), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Resultado", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(output.ifBlank { "Aquí aparecerá tu texto transformado." }, color = if (output.isBlank()) Muted else Color.White)
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            if (output.isNotBlank()) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Texto revisado", output))
                                Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
                            }
                        }, modifier = Modifier.weight(1f)) { Text("Copiar") }
                        Button(onClick = { saveDocx.launch("texto-revisado.docx") }, modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text("Exportar Word") }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("El procesamiento se realiza en este dispositivo, sin enviar tus documentos a servidores. Revisa siempre el resultado y conserva tu propia voz.", color = Muted, fontSize = 12.sp)
        }
    }
}

private fun readDocx(context: Context, uri: Uri): String {
    context.contentResolver.openInputStream(uri).use { stream ->
        requireNotNull(stream) { "Archivo vacío" }.use {
            val doc = XWPFDocument(it)
            return doc.paragraphs.joinToString("\n") { p -> p.text }
        }
    }
}

private fun writeDocx(context: Context, uri: Uri, text: String) {
    context.contentResolver.openOutputStream(uri).use { stream ->
        requireNotNull(stream) { "No se pudo crear el archivo" }.use {
            val doc = XWPFDocument()
            text.split("\n").forEach { line -> doc.createParagraph().createRun().setText(line) }
            doc.write(it)
            doc.close()
        }
    }
}

