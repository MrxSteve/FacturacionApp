package app.models.dao;

import app.models.entity.Factura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface IFacturaDao extends CrudRepository<Factura, Long> {

    @Query("select f from Factura f join fetch f.cliente c join fetch f.items l join fetch l.producto where f.id=?1")
    public Factura fetchByIdWithClienteWithItemFacturaWithProducto(Long id);

    Page<Factura> findByClienteId(Long clienteId, Pageable pageable);

    @Query("select f from Factura f where f.createAt between :startDate and :endDate")
    Page<Factura> findByDateRange(Date startDate, Date endDate, Pageable pageable);

    List<Factura> findAllByCreateAtBetween(Date startDate, Date endDate);


}
