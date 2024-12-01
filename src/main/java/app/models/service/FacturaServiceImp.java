package app.models.service;

import app.models.dao.IFacturaDao;
import app.models.entity.Factura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class FacturaServiceImp implements IFacturaService {
    @Autowired
    private IFacturaDao facturaDao;

    @Override
    @Transactional(readOnly = true)
    public Page<Factura> findByDateRange(Date startDate, Date endDate, Pageable pageable) {
        return facturaDao.findByDateRange(startDate, endDate, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Factura> findAllByDateRange(Date startDate, Date endDate) {
        return facturaDao.findAllByCreateAtBetween(startDate, endDate);
    }
}
