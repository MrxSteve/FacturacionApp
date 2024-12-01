package app.view.pdf;

import app.models.entity.Factura;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import java.awt.*;
import java.util.List;
import java.util.Map;

@Component("factura/rango")
public class FacturasPdfView extends AbstractPdfView {

    @Override
    public void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
                                    HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<Factura> facturas = (List<Factura>) model.get("facturas");
        Integer cantidadFacturas = (Integer) model.get("cantidadFacturas");
        Double totalFacturas = (Double) model.get("totalFacturas");

        // Título del documento
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLUE);
        Paragraph title = new Paragraph("Registro de Facturas", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph("\n"));

        // Crear tabla de facturas con colores
        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidths(new float[]{1, 3, 2, 2});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10);

        // Encabezados de la tabla con colores
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
        PdfPCell cell;

        cell = new PdfPCell(new Phrase("ID", headerFont));
        cell.setBackgroundColor(new Color(0, 102, 204));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(cell);

        cell = new PdfPCell(new Phrase("Cliente", headerFont));
        cell.setBackgroundColor(new Color(0, 102, 204));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(cell);

        cell = new PdfPCell(new Phrase("Fecha", headerFont));
        cell.setBackgroundColor(new Color(0, 102, 204));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(cell);

        cell = new PdfPCell(new Phrase("Total", headerFont));
        cell.setBackgroundColor(new Color(0, 102, 204));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(cell);

        // Agregar los datos de las facturas en la tabla
        Font dataFont = new Font(Font.HELVETICA, 11, Font.NORMAL, Color.BLACK);
        for (Factura factura : facturas) {
            cell = new PdfPCell(new Phrase(String.valueOf(factura.getId()), dataFont));
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(cell);

            cell = new PdfPCell(new Phrase(factura.getCliente().getNombre() + " " + factura.getCliente().getApellido(), dataFont));
            cell.setPadding(5);
            tabla.addCell(cell);

            cell = new PdfPCell(new Phrase(factura.getCreateAt().toString(), dataFont));
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(cell);

            cell = new PdfPCell(new Phrase("$" + factura.getTotal(), dataFont));
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabla.addCell(cell);
        }

        document.add(tabla);

        document.add(new Paragraph("\n"));

        // Añadir información adicional (cantidad de facturas y total)
        Font summaryFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.DARK_GRAY);
        Paragraph cantidad = new Paragraph("Cantidad de facturas encontradas: " + cantidadFacturas, summaryFont);
        cantidad.setAlignment(Element.ALIGN_LEFT);
        document.add(cantidad);

        Paragraph total = new Paragraph("Total de todas las facturas: $" + totalFacturas, summaryFont);
        total.setAlignment(Element.ALIGN_LEFT);
        document.add(total);
    }
}
