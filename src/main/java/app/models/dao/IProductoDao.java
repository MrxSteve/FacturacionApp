package app.models.dao;

import app.models.entity.Producto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface IProductoDao extends CrudRepository<Producto, Long>, PagingAndSortingRepository<Producto, Long> {
    @Query("select p from Producto p where p.nombre like %?1%")
    public List<Producto> findByNombre(String term);

    public List<Producto> findByNombreLikeIgnoreCase(String term);

}
