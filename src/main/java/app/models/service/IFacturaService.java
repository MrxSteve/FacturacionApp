package app.models.service;

import app.models.entity.Factura;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface IFacturaService {
    Page<Factura> findByDateRange(Date startDate, Date endDate, Pageable pageable);


    List<Factura> findAllByDateRange(Date startDate, Date endDate);
}
