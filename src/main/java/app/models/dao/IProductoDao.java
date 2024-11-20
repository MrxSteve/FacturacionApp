package app.models.dao;

import app.models.entity.Producto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface IProductoDao extends CrudRepository<Producto, Long>, PagingAndSortingRepository<Producto, Long> {

}
