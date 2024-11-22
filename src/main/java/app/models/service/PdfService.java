package app.models.service;

import app.models.entity.Factura;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
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
}
