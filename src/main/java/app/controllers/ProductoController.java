package app.controllers;

import app.models.entity.Producto;
import app.models.service.IProductoService;
import app.models.service.IUploadFileService;
import app.util.paginator.PageRender;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

@Controller
@SessionAttributes("producto")
public class ProductoController {

    private static final String PRODUCTOS_FOLDER = "productos";

    private final IProductoService productoService;
    private final IUploadFileService uploadFileService;

    @Autowired
    public ProductoController(IProductoService productoService, IUploadFileService uploadFileService) {
        this.productoService = productoService;
        this.uploadFileService = uploadFileService;
    }

    @GetMapping(value = "/uploads/productos/{filename:.+}")
    public ResponseEntity<Resource> verFoto(@PathVariable String filename) {
        Resource recurso = null;
        try {
            recurso = uploadFileService.load(filename, PRODUCTOS_FOLDER);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
                .body(recurso);
    }

    @GetMapping(value = "/productos/ver/{id}")
    public String ver(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
        Producto producto = productoService.findOne(id);
        if (producto == null) {
            flash.addFlashAttribute("error", "El producto no existe en la base de datos.");
            return "redirect:/productos/listar";
        }
        model.put("producto", producto);
        model.put("titulo", "Detalle del producto: " + producto.getNombre());
        return "productos/ver";
    }

    @RequestMapping(value = "/productos/listar", method = RequestMethod.GET)
    public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        Pageable pageRequest = PageRequest.of(page, 5);
        Page<Producto> productos = productoService.findAll(pageRequest);

        PageRender<Producto> pageRender = new PageRender<>("/productos/listar", productos);

        model.addAttribute("titulo", "Listado de productos");
        model.addAttribute("productos", productos);
        model.addAttribute("page", pageRender);
        return "productos/listar";
    }

    @RequestMapping(value = "/productos/form")
    public String crear(Map<String, Object> model) {
        Producto producto = new Producto();
        model.put("producto", producto);
        model.put("titulo", "Formulario de Producto");
        return "productos/form";
    }

    @RequestMapping(value = "/productos/form", method = RequestMethod.POST)
    public String guardar(@Valid Producto producto, BindingResult result, Model model,
                          @RequestParam("file") MultipartFile foto, RedirectAttributes flash, SessionStatus status) {

        if (result.hasErrors()) {
            model.addAttribute("titulo", "Formulario de Producto");
            return "productos/form";
        }
        if (!foto.isEmpty()) {
            if (producto.getId() != null && producto.getId() > 0 && producto.getFoto() != null
                    && producto.getFoto().length() > 0) {
                uploadFileService.delete(producto.getFoto(), PRODUCTOS_FOLDER);
            }

            String uniqueFilename = null;
            try {
                uniqueFilename = uploadFileService.copy(foto, PRODUCTOS_FOLDER);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            flash.addFlashAttribute("info", "Has subido correctamente '" + uniqueFilename + "'");
            producto.setFoto(uniqueFilename);
        }

        String mensajeFlash = (producto.getId() != null) ? "Producto editado con éxito" : "Producto creado con éxito";

        productoService.save(producto);
        status.setComplete();
        flash.addFlashAttribute("success", mensajeFlash);
        return "redirect:/productos/listar";
    }

    @RequestMapping(value = "/productos/form/{id}")
    public String editar(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
        Producto producto = null;

        if (id > 0) {
            producto = productoService.findOne(id);
            if (producto == null) {
                flash.addFlashAttribute("error", "El ID del producto no existe en la base de datos.");
                return "redirect:/productos/listar";
            }
        } else {
            flash.addFlashAttribute("error", "El ID del producto no puede ser cero.");
            return "redirect:/productos/listar";
        }

        model.put("producto", producto);
        model.put("titulo", "Editar Producto");

        return "productos/form";
    }

    @RequestMapping(value = "/productos/eliminar/{id}")
    public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
        if (id > 0) {
            Producto producto = productoService.findOne(id);
            productoService.delete(id);
            flash.addFlashAttribute("success", "Producto eliminado con éxito");

            if (producto.getFoto() != null) {
                if (uploadFileService.delete(producto.getFoto(), PRODUCTOS_FOLDER)) {
                    flash.addFlashAttribute("info", "Foto " + producto.getFoto() + " eliminada con éxito");
                }
            }
        }
        return "redirect:/productos/listar";
    }
}
