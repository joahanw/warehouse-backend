package com.johanwork.warehouse.transaction.controller;

import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.transaction.dto.request.ConfirmPaymentRequest;
import com.johanwork.warehouse.transaction.dto.request.TransactionRequest;
import com.johanwork.warehouse.transaction.dto.response.*;
import com.johanwork.warehouse.transaction.service.ITransactionProductService;
import com.johanwork.warehouse.transaction.service.ITransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {

    private final ITransactionService transactionService;
    private final ITransactionProductService transactionProductService;

//    @GetMapping(path = "/dashboard", version = "1.0")
//    public ResponseEntity<GenericResponse<DashboardStatsResponse>> getDashboardManager(){
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(transactionService.getDashboardStats());
//    }

    @GetMapping(path = "/dashboard", version = "1.0")
    public ResponseEntity<GenericResponse<DashboardStatsByMerchantResponse>> getDashboard(Authentication authentication){
        return ResponseEntity.status(HttpStatus.OK)
                .body(transactionService.getDashboardStats(authentication.getName()));
    }

    @GetMapping(version = "1.0")
    public ResponseEntity<GenericResponse<PageResponse<TransactionResponse>>> getTransaction(
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long merchantId
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(transactionService.getAllTransaction(pageNumber, pageSize, sortBy,
                        sortDirection, search, merchantId));
    }

    @GetMapping(path = "/{transactionId}", version = "1.0")
    public ResponseEntity<GenericResponse<TransactionProductResponse>> getTransactionProduct(@PathVariable Long transactionId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(transactionProductService.getByTransactionId(transactionId));
    }

    @PostMapping(version = "1.0")
    public ResponseEntity<GenericResponse<CreateTransactionResponse>> createTransactionProduct(@RequestBody @Valid TransactionRequest request){
        return ResponseEntity.status(HttpStatus.OK)
                .body(transactionService.create(request));
    }

    @GetMapping(path = "/{transactionId}/qr-image")
    public ResponseEntity<byte[]> getQrImage(@PathVariable Long transactionId){
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(transactionService.getQrImage(transactionId));
    }

    @PostMapping(path = "/{transactionId}/confirm-payment", version = "1.0")
    public ResponseEntity<GenericResponse<Void>> confirmPayment(@PathVariable Long transactionId,
                                                                  @RequestBody @Valid ConfirmPaymentRequest request,
                                                                  Authentication authentication){
        return ResponseEntity.status(HttpStatus.OK)
                .body(transactionService.confirmPayment(transactionId, request, authentication.getName()));
    }

}
