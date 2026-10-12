package com.lostfound.member4_claims.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * QR Code Generator Utility using Google ZXing.
 * Generates secure QR tokens rendered as Base64 Data URLs for seamless JSP display.
 */
public class QRCodeUtil {

    private QRCodeUtil() {
        // Utility class
    }

    /**
     * Generates a PNG byte array for the specified text.
     */
    public static byte[] generateQRCodeBytes(String text, int width, int height) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("QR Code content cannot be empty.");
        }

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        try (ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        }
    }

    /**
     * Generates a Base64 data URI string suitable for HTML <img src="..."> tags.
     */
    public static String generateQRCodeBase64(String text, int width, int height) {
        try {
            byte[] bytes = generateQRCodeBytes(text, width, height);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return null;
        }
    }
}
