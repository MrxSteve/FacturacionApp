package app.controllers;

import app.models.entity.Cliente;
import app.models.service.IClienteSevice;
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
@SessionAttributes("cliente")
public class ClienteController {

    private static final String CLIENTES_FOLDER = "clientes";

    private final IClienteSevice clienteSevice;
    private final IUploadFileService uploadFileService;

    @Autowired
    public ClienteController(IClienteSevice clienteSevice, IUploadFileService uploadFileService) {
        this.clienteSevice = clienteSevice;
        this.uploadFileService = uploadFileService;
    }

    @GetMapping(value = "/uploads/clientes/{filename:.+}")
    public ResponseEntity<Resource> verFoto(@PathVariable String filename) {
        Resource recurso = null;
        try {
            recurso = uploadFileService.load(filename, CLIENTES_FOLDER);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
                .body(recurso);
    }

    @GetMapping(value = "/ver/{id}")
    public String ver(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
        Cliente cliente = clienteSevice.fetchByIdWithFacturas(id); // clienteSevice.findOne(id);
        if (cliente == null) {
            flash.addFlashAttribute("error", "El cliente no existe en la BD");
            return "redirect:/listar";
        }
        model.put("cliente", cliente);
        model.put("titulo", "Detalle cliente: " + cliente.getNombre());
        return "ver";
    }

    @RequestMapping(value = "/listar", method = RequestMethod.GET)
    public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
        Pageable pageRequest = PageRequest.of(page, 5);
        Page<Cliente> clientes = clienteSevice.findAll(pageRequest);

        PageRender<Cliente> pageRender = new PageRender<>("/listar", clientes);

        model.addAttribute("titulo", "Listado de clientes");
        model.addAttribute("clientes", clientes);
        model.addAttribute("page", pageRender);
        return "listar";
    }

    @RequestMapping(value = "/form")
    public String crear(Map<String, Object> model) {
        Cliente cliente = new Cliente();
        model.put("cliente", cliente);
        model.put("titulo", "Formulario de Cliente");
        return "form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public String guardar(@Valid Cliente cliente, BindingResult result, Model model,
                          @RequestParam("file") MultipartFile foto, RedirectAttributes flash, SessionStatus status) {

        if (result.hasErrors()) {
            model.addAttribute("titulo", "Formulario de Cliente");
            return "form";
        }
        if (!foto.isEmpty()) {
            if (cliente.getId() != null && cliente.getId() > 0 && cliente.getFoto() != null
                    && cliente.getFoto().length() > 0) {
                uploadFileService.delete(cliente.getFoto(), CLIENTES_FOLDER);
            }

            String uniqueFilename = null;
            try {
                uniqueFilename = uploadFileService.copy(foto, CLIENTES_FOLDER);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            flash.addFlashAttribute("info", "Has subido correctamente '" + uniqueFilename + "'");
            cliente.setFoto(uniqueFilename);
        }

        String mensajeFlash = (cliente.getId() != null) ? "Cliente editado con éxito" : "Cliente creado con éxito";

        clienteSevice.save(cliente);
        status.setComplete();
        flash.addFlashAttribute("success", mensajeFlash);
        return "redirect:listar";
    }

    @RequestMapping(value = "/form/{id}")
    public String editar(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
        Cliente cliente = null;

        if (id > 0) {
            cliente = clienteSevice.findOne(id);
            if (cliente == null) {
                flash.addFlashAttribute("error", "El ID del cliente no existe en la BD!");
                return "redirect:/listar";
            }
        } else {
            flash.addFlashAttribute("error", "El ID del cliente no puede ser cero!");
            return "redirect:/listar";
        }

        model.put("cliente", cliente);
        model.put("titulo", "Editar Cliente");

        return "form";
    }

    @RequestMapping(value = "/eliminar/{id}")
    public String eliminar(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
        if (id > 0) {
            Cliente cliente = clienteSevice.findOne(id);
            clienteSevice.delete(id);
            flash.addFlashAttribute("success", "Cliente eliminado con éxito");

            if (cliente.getFoto() != null) {
                if (uploadFileService.delete(cliente.getFoto(), CLIENTES_FOLDER)) {
                    flash.addFlashAttribute("info", "Foto " + cliente.getFoto() + " eliminada con éxito");
                }
            }
        }
        return "redirect:/listar";
    }
}
