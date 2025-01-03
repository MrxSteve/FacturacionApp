package app.controllers;

import app.models.entity.Cliente;
import app.models.entity.DetalleFactura;
import app.models.entity.Factura;
import app.models.entity.Producto;
import app.models.service.IClienteSevice;
import app.models.service.EmailService;
import app.models.service.IFacturaService;
import app.models.service.PdfService;
import app.util.paginator.PageRender;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.ParseException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@SessionAttributes("factura")
public class FacturaController {

    private final IClienteSevice clienteService;
    private final EmailService emailService;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final PdfService pdfService;
    private final IFacturaService facturaService;

    @Autowired
    public FacturaController(IClienteSevice clienteService, EmailService emailService, PdfService pdfService, IFacturaService facturaService) {
        this.clienteService = clienteService;
        this.emailService = emailService;
        this.pdfService = pdfService;
        this.facturaService = facturaService;
    }


    @GetMapping("/factura/ver/{id}")
    public String ver(@PathVariable(value = "id") Long id,
                      Model model,
                      RedirectAttributes flash) {
        Factura factura = clienteService.fetchByIdWithClienteWithItemFacturaWithProducto(id);

        if (factura == null) {
            flash.addFlashAttribute("error", "La factura no existe en la base de datos!");
            return "redirect:/listar";
        }

        model.addAttribute("factura", factura);
        model.addAttribute("titulo", "Factura: ".concat(factura.getDescripcion()));

        return "factura/ver";
    }

    @GetMapping("/factura/form/{clienteId}")
    public String crear(@PathVariable(value = "clienteId") Long clienteId,
                        Map<String, Object> model,
                        RedirectAttributes flash) {
        Cliente cliente = clienteService.findOne(clienteId);

        if (cliente == null) {
            flash.addFlashAttribute("error", "El cliente no existe en la base de datos");
            return "redirect:/listar";
        }

        Factura factura = new Factura();
        factura.setCliente(cliente);

        model.put("factura", factura);
        model.put("titulo", "Crear Factura");

        return "factura/form";
    }

    @GetMapping(value = "/factura/cargar-productos/{term}", produces = {"application/json"})
    public @ResponseBody List<Producto> cargarProductos(@PathVariable String term) {
        return clienteService.findByNombre(term);
    }

    @PostMapping("/factura/form")
    public String guardar(@Valid Factura factura,
                          BindingResult result,
                          Model model,
                          @RequestParam(name = "item_id[]", required = false) Long[] itemId,
                          @RequestParam(name = "cantidad[]", required = false) Integer[] cantidad,
                          RedirectAttributes flash,
                          SessionStatus status) {

        if (result.hasErrors()) {
            model.addAttribute("titulo", "Crear Factura");
            return "factura/form";
        }

        if (itemId == null || itemId.length == 0) {
            model.addAttribute("titulo", "Crear Factura");
            model.addAttribute("error", "Error: La factura NO puede no tener líneas!");
            return "factura/form";
        }

        for (int i = 0; i < itemId.length; i++) {
            Producto producto = clienteService.findProductoById(itemId[i]);

            DetalleFactura linea = new DetalleFactura();
            linea.setCantidad(cantidad[i]);
            linea.setProducto(producto);
            factura.addItemFactura(linea);

            log.info("ID: " + itemId[i] + ", cantidad: " + cantidad[i]);
        }

        clienteService.saveFactura(factura);
        status.setComplete();

        // Generar PDF
        byte[] pdfBytes = pdfService.generarFacturaPdf(factura);

        // Enviar correo con el PDF adjunto
        String to = factura.getCliente().getEmail();
        String subject = "Factura generada - " + factura.getDescripcion();
        StringBuilder text = new StringBuilder();
        text.append("Hola ").append(factura.getCliente().getNombre()).append(",\n\n")
                .append("Se adjunta la factura generada para su compra.\n\n")
                .append("Gracias por su preferencia.\n\n")
                .append("Saludos,\nDevCode");

        try {
            emailService.sendEmailWithAttachment(to, subject, text.toString(), pdfBytes, "Factura_" + factura.getId() + ".pdf");
            flash.addFlashAttribute("success", "Factura creada y enviada por correo con éxito!");
        } catch (Exception e) {
            log.error("Error enviando el correo: ", e);
            flash.addFlashAttribute("error", "Factura creada, pero no se pudo enviar el correo.");
        }

        return "redirect:/ver/" + factura.getCliente().getId();
    }


    @GetMapping("/factura/eliminar/{id}")
    public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
        Factura factura = clienteService.findFacturaById(id);

        if (factura != null) {
            clienteService.deleteFactura(id);
            flash.addFlashAttribute("success", "Factura eliminada con éxito!");
            return "redirect:/ver/" + factura.getCliente().getId();
        }
        flash.addFlashAttribute("error", "La factura no existe en la base de datos, no se pudo eliminar!");

        return "redirect:/listar";
    }


    @GetMapping("/factura/rango")
    public String listarFacturasPorRango(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        Pageable pageRequest = PageRequest.of(page, 5);
        Page<Factura> facturas = Page.empty();
        double totalFacturas = 0;
        long cantidadFacturas = 0;

        if (startDate != null && endDate != null) {
            // Obtener las facturas paginadas
            facturas = facturaService.findByDateRange(startDate, endDate, pageRequest);

            // Obtener todas las facturas en el rango (sin paginación)
            List<Factura> todasFacturas = facturaService.findAllByDateRange(startDate, endDate);

            // Sumar el total de todas las facturas utilizando el método getTotal()
            totalFacturas = todasFacturas.stream().mapToDouble(Factura::getTotal).sum();

            // Cantidad total de facturas encontradas
            cantidadFacturas = facturas.getTotalElements();
        }

        PageRender<Factura> pageRender = new PageRender<>("/factura/rango", facturas);

        model.addAttribute("titulo", "Listado de facturas por rango de fechas");
        model.addAttribute("facturas", facturas);
        model.addAttribute("page", pageRender);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("totalFacturas", totalFacturas);
        model.addAttribute("cantidadFacturas", cantidadFacturas);

        return "factura/rango";
    }

    @GetMapping("/factura/rango/pdf")
    public void exportarFacturasPdf(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            HttpServletResponse response) {

        Pageable pageRequest = PageRequest.of(0, Integer.MAX_VALUE); // Obtenemos todas las facturas en el rango
        Page<Factura> facturasPage = facturaService.findByDateRange(startDate, endDate, pageRequest);

        List<Factura> facturas = facturasPage.getContent();
        Integer cantidadFacturas = facturas.size();
        Double totalFacturas = facturas.stream().mapToDouble(Factura::getTotal).sum();

        try {
            byte[] pdfBytes = pdfService.generarFacturasPdf(facturas, cantidadFacturas, totalFacturas);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Facturas_Rango.pdf");
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
