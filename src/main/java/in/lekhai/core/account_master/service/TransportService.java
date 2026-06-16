package in.lekhai.core.account_master.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.Transport;
import in.lekhai.core.account_master.repository.TransportRepository;
import in.lekhai.core.account_master.utils.DateUtils;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class TransportService {

    private final TransportRepository transportRepository;

    public TransportService(TransportRepository transportRepository) {
        this.transportRepository = transportRepository;
    }

    @ShopContextTransactional
    public TransportResponse createTransport(TransportRequest request) {
        Transport transport = new Transport(
                request.getName(),
                request.getPhone(),
                request.getGstNo());
        Transport saved = transportRepository.save(transport);
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public List<DropdownItem> listTransports() {
        return StreamSupport.stream(transportRepository.findAll().spliterator(), false)
                .map(transport -> new DropdownItem().id(transport.getId()).label(transport.getName()))
                .toList();
    }

    @ShopContextTransactional
    public TransportSummaryPageResponse listTransportSummaries(TransportSearchableField searchableField, String query, Pageable pageable) {
        Page<Transport> transportsPage;
        if (query != null && !query.trim().isEmpty()) {
            if (searchableField == TransportSearchableField.NAME) {
                List<Transport> transports = transportRepository.findByNameContainingIgnoreCase(query.trim(), pageable);
                long total = transportRepository.countByNameContainingIgnoreCase(query.trim());
                transportsPage = new PageImpl<>(transports, pageable, total);
            } else if (searchableField == TransportSearchableField.GST_NO) {
                List<Transport> transports = transportRepository.findByGstNoContainingIgnoreCase(query.trim(), pageable);
                long total = transportRepository.countByGstNoContainingIgnoreCase(query.trim());
                transportsPage = new PageImpl<>(transports, pageable, total);
            } else {
                transportsPage = transportRepository.findAll(pageable);
            }
        } else {
            transportsPage = transportRepository.findAll(pageable);
        }

        List<TransportSummaryItem> data = transportsPage.getContent().stream()
                .map(transport -> new TransportSummaryItem()
                        .id(transport.getId())
                        .name(transport.getName())
                        .phone(transport.getPhone())
                        .gstNo(transport.getGstNo()))
                .toList();

        return new TransportSummaryPageResponse()
                .data(data)
                .pagination(new PaginationMeta()
                        .page(transportsPage.getNumber())
                        .size(transportsPage.getSize())
                        .totalElements(transportsPage.getTotalElements())
                        .totalPages(transportsPage.getTotalPages()));
    }

    private TransportResponse mapToResponse(Transport transport) {
        return new TransportResponse()
                .id(transport.getId())
                .name(transport.getName())
                .phone(transport.getPhone())
                .gstNo(transport.getGstNo())
                .createdAt(DateUtils.getCreatedAt(transport.getCreatedAt()));
    }
}
