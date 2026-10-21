package cl.belmonte.app.qr

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Analizador de fotogramas de CameraX con Google ML Kit Barcode Scanning (Fase 4).
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Recibe cada fotograma en tiempo real desde la cámara del dispositivo (ImageProxy),
 * lo convierte en un objeto InputImage de ML Kit y busca códigos QR (FORMAT_QR_CODE).
 * Cuando detecta un código QR válido (por ejemplo "KR-TW-21" o "LK-JH-44"), invoca el
 * callback onQrDetectado(valorQr) hacia la pantalla Compose.
 *
 * ¿POR QUÉ SE SEPARA EN SU PROPIO ARCHIVO DENTRO DEL PAQUETE qr?
 * Para que durante la defensa técnica sea muy fácil ubicar dónde ocurre el procesamiento
 * de ML Kit (QrAnalyzer.kt) separado de la interfaz gráfica de la cámara (EscanearQrScreen.kt).
 */
class QrAnalyzer(
    private val onQrDetectado: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // Configuramos ML Kit para escanear exclusivamente códigos QR (más rápido y preciso)
    private val opcionesScanner = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    private val scanner = BarcodeScanning.getClient(opcionesScanner)

    // Evita disparar múltiples lecturas idénticas por segundo del mismo QR
    private var ultimoQrLeido: String? = null
    private var ultimoTiempoLecturaMs: Long = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        // Convertimos el fotograma de CameraX al formato que entiende ML Kit respetando la rotación
        val imagenEntrada = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(imagenEntrada)
            .addOnSuccessListener { codigosDetectados ->
                val primerQr = codigosDetectados.firstOrNull()?.rawValue?.trim()
                if (!primerQr.isNullOrEmpty()) {
                    val ahora = System.currentTimeMillis()
                    // Permitimos re-lectura si cambió el QR o si pasaron más de 2.5 segundos
                    if (primerQr != ultimoQrLeido || (ahora - ultimoTiempoLecturaMs) > 2500L) {
                        ultimoQrLeido = primerQr
                        ultimoTiempoLecturaMs = ahora
                        onQrDetectado(primerQr)
                    }
                }
            }
            .addOnCompleteListener {
                // OBLIGATORIO en CameraX: cerrar siempre el imageProxy para recibir el siguiente fotograma
                imageProxy.close()
            }
    }
}
