package app.models.service;

import app.models.entity.Factura;
import com.lowagie.text.Document;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {

    public byte[] generarFacturaPdf(Factura factura) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            new app.view.pdf.FacturaPdfView().buildPdfDocument(Map.of("factura", factura), document, null, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    public byte[] generarFacturasPdf(List<Factura> facturas, Integer cantidadFacturas, Double totalFacturas) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Llamar a la nueva view para generar el PDF
            new app.view.pdf.FacturasPdfView().buildPdfDocument(
                    Map.of("facturas", facturas, "cantidadFacturas", cantidadFacturas, "totalFacturas", totalFacturas),
                    document,
                    null,
                    null,
                    null
            );

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

}
