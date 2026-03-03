package com.example.inventory.Services;

import com.example.inventory.Model.Inventory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    // ═══════════════════════════════════════════════════════════
    //  COLOR PALETTE
    // ═══════════════════════════════════════════════════════════
    private static final DeviceRgb PRIMARY_DARK  = new DeviceRgb(26, 26, 46);     // #1a1a2e
    private static final DeviceRgb PRIMARY_BLUE  = new DeviceRgb(67, 97, 238);     // #4361ee
    private static final DeviceRgb ACCENT_BLUE   = new DeviceRgb(72, 149, 239);    // #4895ef
    private static final DeviceRgb LIGHT_BG      = new DeviceRgb(248, 249, 252);   // #f8f9fc
    private static final DeviceRgb STRIPE_ROW    = new DeviceRgb(240, 242, 247);   // #f0f2f7
    private static final DeviceRgb BORDER_LIGHT  = new DeviceRgb(222, 226, 235);   // #dee2eb
    private static final DeviceRgb TEXT_DARK     = new DeviceRgb(33, 37, 41);      // #212529
    private static final DeviceRgb TEXT_MUTED    = new DeviceRgb(108, 117, 125);   // #6c757d
    private static final DeviceRgb SUCCESS_GREEN = new DeviceRgb(6, 214, 160);     // #06d6a0
    private static final DeviceRgb HIGHLIGHT_BOX = new DeviceRgb(232, 240, 254);   // #e8f0fe

    // ═══════════════════════════════════════════════════════════
    //  INVENTORY REPORT PDF (All Items)
    // ═══════════════════════════════════════════════════════════

    public byte[] generateInventoryReportPdf(List<Inventory> inventoryItems) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(40, 40, 60, 40);

            // ── Company Header ──
            addCompanyHeader(document);

            // ── Report Title Bar ──
            Table titleBar = new Table(UnitValue.createPercentArray(new float[]{3, 2}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(20)
                    .setMarginBottom(10);

            titleBar.addCell(new Cell().add(new Paragraph("INVENTORY REPORT")
                            .setFontSize(22).setBold().setFontColor(PRIMARY_DARK))
                    .setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
            titleBar.addCell(new Cell().add(new Paragraph("Generated: " + dateStr)
                            .setFontSize(10).setFontColor(TEXT_MUTED).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));

            document.add(titleBar);

            // ── Thin Separator ──
            addSeparator(document);

            // ── Report Summary Header ──
            double totalValue = inventoryItems.stream()
                    .mapToDouble(i -> i.getQTY() * i.getPrice())
                    .sum();
            double avgPrice = inventoryItems.isEmpty() ? 0 :
                    inventoryItems.stream().mapToDouble(Inventory::getPrice).average().orElse(0);
            int totalQty = inventoryItems.stream().mapToInt(Inventory::getQTY).sum();

            Table summaryCards = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(15)
                    .setMarginBottom(20);

            summaryCards.addCell(createSummaryCard("Total Items", String.valueOf(inventoryItems.size())));
            summaryCards.addCell(createSummaryCard("Total Units", String.valueOf(totalQty)));
            summaryCards.addCell(createSummaryCard("Total Value", "$" + String.format("%,.2f", totalValue)));
            summaryCards.addCell(createSummaryCard("Avg. Price", "$" + String.format("%,.2f", avgPrice)));

            document.add(summaryCards);

            // ── Data Table ──
            Table table = new Table(UnitValue.createPercentArray(new float[]{0.5f, 2.5f, 1.5f, 1f, 1.5f, 1.5f}))
                    .setWidth(UnitValue.createPercentValue(100));

            // Table Headers
            String[] headers = {"#", "Item Name", "PLU Code", "Qty", "Unit Price", "Total Value"};
            for (String header : headers) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(header).setFontSize(10).setBold().setFontColor(ColorConstants.WHITE))
                        .setBackgroundColor(PRIMARY_DARK)
                        .setPadding(10)
                        .setBorder(new SolidBorder(PRIMARY_DARK, 0.5f))
                        .setTextAlignment(header.equals("#") || header.equals("Item Name") || header.equals("PLU Code")
                                ? TextAlignment.LEFT : TextAlignment.RIGHT));
            }

            // Table Data
            int rowIndex = 0;
            for (Inventory item : inventoryItems) {
                rowIndex++;
                DeviceRgb rowBg = (rowIndex % 2 == 0) ? STRIPE_ROW : new DeviceRgb(255, 255, 255);
                double itemTotal = item.getQTY() * item.getPrice();

                table.addCell(createDataCell(String.valueOf(rowIndex), rowBg, TextAlignment.LEFT));
                table.addCell(createDataCell(item.getItemName() != null ? item.getItemName() : "N/A", rowBg, TextAlignment.LEFT));
                table.addCell(createDataCell(item.getPLU(), rowBg, TextAlignment.LEFT));
                table.addCell(createDataCell(String.valueOf(item.getQTY()), rowBg, TextAlignment.RIGHT));
                table.addCell(createDataCell("$" + String.format("%,.2f", item.getPrice()), rowBg, TextAlignment.RIGHT));
                table.addCell(createDataCell("$" + String.format("%,.2f", itemTotal), rowBg, TextAlignment.RIGHT));
            }

            document.add(table);

            // ── Grand Total Row ──
            Table totalRow = new Table(UnitValue.createPercentArray(new float[]{7, 1.5f}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(2);

            totalRow.addCell(new Cell()
                    .add(new Paragraph("GRAND TOTAL").setFontSize(11).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(PRIMARY_BLUE)
                    .setPadding(10)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT));

            totalRow.addCell(new Cell()
                    .add(new Paragraph("$" + String.format("%,.2f", totalValue))
                            .setFontSize(11).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(PRIMARY_BLUE)
                    .setPadding(10)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT));

            document.add(totalRow);

            // ── Footer ──
            addFooter(document);

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    // ═══════════════════════════════════════════════════════════
    //  SINGLE ITEM PDF (Product Card)
    // ═══════════════════════════════════════════════════════════

    public byte[] generateSingleInventoryItemPdf(Inventory item) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(PageSize.A4);
            Document document = new Document(pdfDoc, PageSize.A4);
            document.setMargins(40, 40, 60, 40);

            // ── Company Header ──
            addCompanyHeader(document);

            // ── Document Title ──
            Table titleBar = new Table(UnitValue.createPercentArray(new float[]{3, 2}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(20)
                    .setMarginBottom(10);

            titleBar.addCell(new Cell().add(new Paragraph("PRODUCT DETAILS")
                            .setFontSize(22).setBold().setFontColor(PRIMARY_DARK))
                    .setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));

            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
            titleBar.addCell(new Cell().add(new Paragraph("Generated: " + dateStr)
                            .setFontSize(10).setFontColor(TEXT_MUTED).setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));

            document.add(titleBar);
            addSeparator(document);

            // ── Product Info Card ──
            Div cardDiv = new Div()
                    .setBackgroundColor(LIGHT_BG)
                    .setPadding(25)
                    .setMarginTop(20)
                    .setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(8));

            cardDiv.add(new Paragraph(item.getItemName() != null ? item.getItemName() : "N/A")
                    .setFontSize(24).setBold().setFontColor(PRIMARY_DARK).setMarginBottom(5));
            cardDiv.add(new Paragraph("PLU: " + item.getPLU())
                    .setFontSize(12).setFontColor(TEXT_MUTED).setMarginBottom(20));

            document.add(cardDiv);

            // ── Details Table ──
            Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{1.5f, 3f}))
                    .setWidth(UnitValue.createPercentValue(70))
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    .setMarginTop(25);

            addDetailRow(detailsTable, "Item ID", String.valueOf(item.getInventoryID()), false);
            addDetailRow(detailsTable, "Item Name", item.getItemName() != null ? item.getItemName() : "N/A", true);
            addDetailRow(detailsTable, "PLU Code", item.getPLU(), false);
            addDetailRow(detailsTable, "Quantity", item.getQTY() + " units", true);
            addDetailRow(detailsTable, "Unit Price", "$" + String.format("%,.2f", item.getPrice()), false);

            document.add(detailsTable);

            // ── Total Value Highlight ──
            double totalValue = item.getQTY() * item.getPrice();

            Table totalBox = new Table(1)
                    .setWidth(UnitValue.createPercentValue(70))
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    .setMarginTop(5);

            Cell totalCell = new Cell()
                    .setBackgroundColor(PRIMARY_BLUE)
                    .setPadding(15)
                    .setBorder(Border.NO_BORDER);

            Table innerTotal = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100));

            innerTotal.addCell(new Cell().add(new Paragraph("TOTAL VALUE")
                            .setFontSize(14).setBold().setFontColor(ColorConstants.WHITE))
                    .setBorder(Border.NO_BORDER));
            innerTotal.addCell(new Cell().add(new Paragraph("$" + String.format("%,.2f", totalValue))
                            .setFontSize(18).setBold().setFontColor(ColorConstants.WHITE)
                            .setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER));

            totalCell.add(innerTotal);
            totalBox.addCell(totalCell);
            document.add(totalBox);

            // ── Status Badge ──
            String status = item.getQTY() > 0 ? "IN STOCK" : "OUT OF STOCK";
            DeviceRgb statusColor = item.getQTY() > 0 ? SUCCESS_GREEN : new DeviceRgb(239, 71, 111);

            document.add(new Paragraph(status)
                    .setFontSize(12)
                    .setBold()
                    .setFontColor(statusColor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20));

            // ── Footer ──
            addFooter(document);

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    private void addCompanyHeader(Document document) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBackgroundColor(PRIMARY_DARK);

        Cell headerCell = new Cell()
                .setBackgroundColor(PRIMARY_DARK)
                .setPadding(20)
                .setBorder(Border.NO_BORDER);

        headerCell.add(new Paragraph("INVENTORY MANAGEMENT SYSTEM")
                .setFontSize(18).setBold().setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(3));
        headerCell.add(new Paragraph("Professional Inventory Tracking & Reporting")
                .setFontSize(10).setFontColor(new DeviceRgb(170, 175, 190))
                .setTextAlignment(TextAlignment.CENTER));

        header.addCell(headerCell);
        document.add(header);
    }

    private void addSeparator(Document document) {
        SolidLine line = new SolidLine(1.5f);
        line.setColor(PRIMARY_BLUE);
        document.add(new LineSeparator(line).setMarginTop(5).setMarginBottom(5));
    }

    private Cell createSummaryCard(String label, String value) {
        Cell cell = new Cell()
                .setBackgroundColor(HIGHLIGHT_BOX)
                .setPadding(12)
                .setBorder(new SolidBorder(BORDER_LIGHT, 0.5f))
                .setTextAlignment(TextAlignment.CENTER);

        cell.add(new Paragraph(label).setFontSize(9).setFontColor(TEXT_MUTED).setMarginBottom(4));
        cell.add(new Paragraph(value).setFontSize(14).setBold().setFontColor(PRIMARY_DARK));

        return cell;
    }

    private Cell createDataCell(String content, DeviceRgb bgColor, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(content).setFontSize(10).setFontColor(TEXT_DARK))
                .setBackgroundColor(bgColor)
                .setPadding(8)
                .setBorder(new SolidBorder(BORDER_LIGHT, 0.3f))
                .setTextAlignment(alignment);
    }

    private void addDetailRow(Table table, String label, String value, boolean isStriped) {
        DeviceRgb bgColor = isStriped ? STRIPE_ROW : new DeviceRgb(255, 255, 255);

        table.addCell(new Cell()
                .add(new Paragraph(label).setFontSize(11).setBold().setFontColor(TEXT_MUTED))
                .setBackgroundColor(bgColor)
                .setPadding(12)
                .setBorder(new SolidBorder(BORDER_LIGHT, 0.3f)));

        table.addCell(new Cell()
                .add(new Paragraph(value).setFontSize(11).setFontColor(TEXT_DARK))
                .setBackgroundColor(bgColor)
                .setPadding(12)
                .setBorder(new SolidBorder(BORDER_LIGHT, 0.3f)));
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("")
                .setMarginTop(30));

        Table footerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(10);

        footerTable.addCell(new Cell()
                .add(new Paragraph("CONFIDENTIAL — For internal use only")
                        .setFontSize(8).setFontColor(TEXT_MUTED))
                .setBorder(Border.NO_BORDER));

        footerTable.addCell(new Cell()
                .add(new Paragraph("\u00A9 2025 Inventory Management System")
                        .setFontSize(8).setFontColor(TEXT_MUTED).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER));

        document.add(footerTable);
    }
}