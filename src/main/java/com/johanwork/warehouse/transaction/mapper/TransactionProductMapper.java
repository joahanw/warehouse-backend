package com.johanwork.warehouse.transaction.mapper;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.product.dto.ProductResponse;
import com.johanwork.warehouse.product.mapper.ProductMapper;
import com.johanwork.warehouse.transaction.dto.response.TransactionProductResponse;
import com.johanwork.warehouse.transaction.entity.TransactionProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionProductMapper {

    private final ProductMapper productMapper;

    public TransactionProductResponse entityToResponse(TransactionProduct tp){
        ProductResponse productResponse = productMapper.mapEntityToResponse(tp.getProduct());
        TransactionProductResponse res = new TransactionProductResponse();
        res.setProduct(productResponse);
        res.setId(tp.getId());
        res.setPrice(tp.getPrice());
        res.setQuantity(tp.getQuantity());
        res.setSubTotal(tp.getSubTotal());
        return res;
    }

    public GenericResponse<PageResponse<TransactionProductResponse>> mapToPageTransactionProductResponse(Page<TransactionProduct> m, String message){
        if(null != m){
            var res = new PageResponse<>(
                    m.map(this::entityToResponse).getContent(),
                    m.getNumber(),
                    m.getSize(),
                    m.getTotalElements(),
                    m.getTotalPages(),
                    m.hasNext(),
                    m.hasPrevious());
            return new GenericResponse<>(res, message);
        }
        return new GenericResponse<>(new PageResponse<>(), message);
    }

    public GenericResponse<TransactionProductResponse> mapToGenericResponse(TransactionProduct tp, String message){
        return new GenericResponse<>(this.entityToResponse(tp), message);
    }

}
