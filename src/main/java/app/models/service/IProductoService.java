package app.models.service;

import app.models.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IProductoService {
    public List<Producto> findAll();
    public Page<Producto> findAll(Pageable pageable);
    public void save(Producto producto);
    public Producto findOne(Long id);
    public void delete(Long id);

    Page<Producto> findByNombreContaining(String nombre, Pageable pageable);
}
