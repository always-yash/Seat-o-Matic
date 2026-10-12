package com.lostfound.member5_admin.util;

import com.lostfound.model.Item;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * PDF Exporter Utility using Apache PDFBox.
 * Generates downloadable institutional audit and inventory reports.
 */
public class PdfExporter {

    private PdfExporter() {
        // Utility class
    }

    /**
     * Generates a PDF report document stream with institutional header, summary metrics,
     * and tabular listings of recent items.
     */
    public static byte[] generateSummaryPdf(Map<String, Object> stats, List<Item> items) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontOblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = 780;

                // Title Banner
                cs.beginText();
                cs.setFont(fontBold, 18);
                cs.newLineAtOffset(50, y);
                cs.showText("Campus Lost & Found Management System");
                cs.endText();

                y -= 22;
                cs.beginText();
                cs.setFont(fontBold, 13);
                cs.newLineAtOffset(50, y);
                cs.showText("Official Executive Summary & Inventory Audit Report");
                cs.endText();

                y -= 18;
                cs.beginText();
                cs.setFont(fontOblique, 10);
                cs.newLineAtOffset(50, y);
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                cs.showText("Generated On: " + timestamp + " | Confidential Campus Records");
                cs.endText();

                // Horizontal separator line
                y -= 15;
                cs.setLineWidth(1.0f);
                cs.moveTo(50, y);
                cs.lineTo(545, y);
                cs.stroke();

                // Section: Executive Summary Metrics
                y -= 25;
                cs.beginText();
                cs.setFont(fontBold, 12);
                cs.newLineAtOffset(50, y);
                cs.showText("1. System Key Metrics");
                cs.endText();

                y -= 20;
                cs.beginText();
                cs.setFont(fontRegular, 10);
                cs.newLineAtOffset(60, y);
                cs.showText("Total Users: " + String.valueOf(stats.getOrDefault("totalUsers", 0)) +
                            "   |   Active Users: " + String.valueOf(stats.getOrDefault("activeUsers", 0)) +
                            "   |   Blocked Users: " + String.valueOf(stats.getOrDefault("blockedUsers", 0)));
                cs.endText();

                y -= 18;
                cs.beginText();
                cs.setFont(fontRegular, 10);
                cs.newLineAtOffset(60, y);
                cs.showText("Total Items Logged: " + String.valueOf(stats.getOrDefault("totalItems", 0)) +
                            "   |   LOST: " + String.valueOf(stats.getOrDefault("lostItems", 0)) +
                            "   |   FOUND: " + String.valueOf(stats.getOrDefault("foundItems", 0)));
                cs.endText();

                y -= 18;
                cs.beginText();
                cs.setFont(fontRegular, 10);
                cs.newLineAtOffset(60, y);
                cs.showText("Returned Items: " + String.valueOf(stats.getOrDefault("returnedItems", 0)) +
                            "   |   Pending Claims: " + String.valueOf(stats.getOrDefault("pendingClaims", 0)) +
                            "   |   Open Moderation Reports: " + String.valueOf(stats.getOrDefault("openReports", 0)));
                cs.endText();

                // Section: Recent Items Table
                y -= 30;
                cs.beginText();
                cs.setFont(fontBold, 12);
                cs.newLineAtOffset(50, y);
                cs.showText("2. Recent Campus Items Registry");
                cs.endText();

                y -= 18;
                // Table header
                cs.beginText();
                cs.setFont(fontBold, 9);
                cs.newLineAtOffset(50, y);
                cs.showText(String.format("%-6s %-30s %-12s %-8s %-12s %-10s", "ID", "TITLE", "CATEGORY", "TYPE", "DATE", "STATUS"));
                cs.endText();

                y -= 6;
                cs.moveTo(50, y);
                cs.lineTo(545, y);
                cs.stroke();

                y -= 14;
                cs.setFont(fontRegular, 9);
                int limit = Math.min(items.size(), 24);
                for (int i = 0; i < limit; i++) {
                    Item it = items.get(i);
                    String rawTitle = it.getTitle() != null ? it.getTitle() : "N/A";
                    String safeTitle = sanitizeForPdf(rawTitle);
                    if (safeTitle.length() > 24) {
                        safeTitle = safeTitle.substring(0, 21) + "...";
                    }
                    String rawCat = it.getCategoryName() != null ? it.getCategoryName() : "General";
                    String cat = sanitizeForPdf(rawCat);
                    if (cat.length() > 10) cat = cat.substring(0, 9) + ".";

                    String dateStr = it.getItemDate() != null ? it.getItemDate().toString() : "-";
                    String line = String.format("#%-5d %-28s %-12s %-8s %-12s %-10s",
                            it.getItemId(), safeTitle, cat, it.getItemType(), dateStr, it.getStatus());

                    cs.beginText();
                    cs.newLineAtOffset(50, y);
                    cs.showText(sanitizeForPdf(line));
                    cs.endText();
                    y -= 14;

                    if (y < 60) break;
                }

                // Footer
                cs.beginText();
                cs.setFont(fontOblique, 8);
                cs.newLineAtOffset(50, 40);
                cs.showText("College Lost & Found Management Portal - Automated Report Generation Engine");
                cs.endText();
            }

            document.save(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Sanitizes strings for PDFBox Type 1 WinAnsi encoding.
     * Prevents IllegalArgumentException when text contains non-ASCII characters,
     * smart quotes, em-dashes, or line breaks.
     */
    public static String sanitizeForPdf(String text) {
        if (text == null) {
            return "";
        }
        String s = text.replace('\u2013', '-')
                       .replace('\u2014', '-')
                       .replace('\u2018', '\'')
                       .replace('\u2019', '\'')
                       .replace('\u201c', '"')
                       .replace('\u201d', '"')
                       .replace('\u2022', '*')
                       .replace('\u2026', '.')
                       .replace('\r', ' ')
                       .replace('\n', ' ')
                       .replace('\t', ' ');

        StringBuilder sb = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (c >= 32 && c <= 126) {
                sb.append(c);
            } else if (c >= 160 && c <= 255) {
                sb.append(c);
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }
}
