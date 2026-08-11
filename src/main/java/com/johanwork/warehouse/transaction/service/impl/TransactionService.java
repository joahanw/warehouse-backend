package com.johanwork.warehouse.transaction.service.impl;

import tools.jackson.databind.ObjectMapper;
import com.johanwork.warehouse.common.config.configProps.BcaQrisProperties;
import com.johanwork.warehouse.common.constant.AppConstant;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.common.response.GenericResponse;
import com.johanwork.warehouse.common.response.PageResponse;
import com.johanwork.warehouse.common.util.QrCodeImageGenerator;
import com.johanwork.warehouse.common.util.QrisUtil;
import com.johanwork.warehouse.merchant.entity.Merchant;
import com.johanwork.warehouse.merchant.service.IMerchantDomainService;
import com.johanwork.warehouse.notification.dto.PaymentPendingDto;
import com.johanwork.warehouse.notification.dto.WhatsAppTemplate;
import com.johanwork.warehouse.notification.entity.NotificationOutbox;
import com.johanwork.warehouse.notification.repository.NotificationOutboxRepository;
import com.johanwork.warehouse.notification.service.INotificationService;
import com.johanwork.warehouse.notification.service.impl.WhatsAppService;
import com.johanwork.warehouse.product.entity.Product;
import com.johanwork.warehouse.product.service.IProductDomainService;
import com.johanwork.warehouse.transaction.dto.PaymentMethod;
import com.johanwork.warehouse.transaction.dto.request.ConfirmPaymentRequest;
import com.johanwork.warehouse.transaction.dto.request.ProductItems;
import com.johanwork.warehouse.transaction.dto.request.QrisChargeRequest;
import com.johanwork.warehouse.transaction.dto.request.TransactionRequest;
import com.johanwork.warehouse.transaction.dto.response.*;
import com.johanwork.warehouse.transaction.entity.Transaction;
import com.johanwork.warehouse.transaction.entity.TransactionProduct;
import com.johanwork.warehouse.transaction.mapper.TransactionMapper;
import com.johanwork.warehouse.transaction.repository.TransactionProductRepository;
import com.johanwork.warehouse.transaction.repository.TransactionRepository;
import com.johanwork.warehouse.transaction.service.IPaymentService;
import com.johanwork.warehouse.transaction.service.ITransactionService;
import com.johanwork.warehouse.transaction.spesification.TransactionSpecification;
import com.johanwork.warehouse.user.entity.User;
import com.johanwork.warehouse.user.service.IUserDomainService;

import tools.jackson.core.JacksonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static com.johanwork.warehouse.common.util.AppUtil.*;

@Service
@Transactional(readOnly = true)
@Slf4j
public class TransactionService implements ITransactionService {

    @Value("${app.baseUrl}")
    private String baseUrl;

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final IMerchantDomainService merchantService;
    private final IProductDomainService productService;
    private final IUserDomainService userService;
    private final TransactionProductRepository transactionProductRepository;
    private final RestClient midtransRestClient;
    private final INotificationService notificationService;
    private final WhatsAppService whatsAppService;
    private final BcaQrisProperties bcaQrisProperties;
    private final IPaymentService paymentService;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final ObjectMapper objectMapper;

    public TransactionService(TransactionRepository transactionRepository,
                              TransactionMapper transactionMapper,
                              IMerchantDomainService merchantService,
                              IProductDomainService productService,
                              TransactionProductRepository transactionProductRepository,
                              @Qualifier("midtransRestClient") RestClient midtransRestClient,
                              INotificationService notificationService,
                              IUserDomainService userService,
                              WhatsAppService whatsAppService,
                              BcaQrisProperties bcaQrisProperties,
                              IPaymentService paymentService,
                              NotificationOutboxRepository notificationOutboxRepository,
                              ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.merchantService = merchantService;
        this.productService = productService;
        this.transactionProductRepository = transactionProductRepository;
        this.midtransRestClient = midtransRestClient;
        this.notificationService = notificationService;
        this.userService = userService;
        this.whatsAppService = whatsAppService;
        this.bcaQrisProperties = bcaQrisProperties;
        this.paymentService = paymentService;
        this.notificationOutboxRepository = notificationOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Value("${transaction.taxRate}")
    private BigDecimal taxRate;

    @Override
    public GenericResponse<DashboardStatsByMerchantResponse> getDashboardStats(String email) {
        User user = userService.findByEmail(email);
        boolean manager = user.getRoles().stream()
                .anyMatch(data -> data.getName().equalsIgnoreCase("manager"));
        if (manager) {
            return transactionMapper.mapToDashboardStatsByMerchantResponse(
                    transactionRepository.getDashboardStats(),
                    String.format(AppConstant.Success.FETCHED, "Dashboard stats for Manager")
            );
        }
        Merchant merchant = merchantService.findMerchantByUserId(user.getId());
        return transactionMapper.mapToDashboardStatsByMerchantResponse(
                transactionRepository.getDashboardStatsByMerchant(merchant.getId()),
                String.format(AppConstant.Success.FETCHED, "Dashboard stats for Manager")
        );

    }

    @Transactional
    @Override
    public GenericResponse<CreateTransactionResponse> create(TransactionRequest rq) {
        Merchant merchant = merchantService.findMerchantById(rq.merchantId());
        String orderId = String.format("ORD-%s-%s",
                Long.toString(Instant.now().toEpochMilli(), 36).toUpperCase(), merchant.getId());

        List<TransactionProduct> items = buildItems(rq.products());
        BigDecimal subTotal = items.stream().map(TransactionProduct::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxTotal = subTotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = subTotal.add(taxTotal).add(rq.shippingCost());

        Transaction transaction = transactionMapper.requestToEntity(rq, merchant, subTotal, taxTotal, grandTotal, orderId);
        Transaction saved = transactionRepository.save(transaction);

        items.forEach(item -> item.setTransaction(saved));
        transactionProductRepository.saveAll(items);

        String qrCodeUrl;
        String expiryTimeDisplay;
        Instant expiryTime;

        if (saved.getPaymentMethod() == PaymentMethod.bca_qris_static) {
            expiryTime = Instant.now().plus(5, ChronoUnit.HOURS);
            String dynamicQris = QrisUtil.injectDynamicAmount(bcaQrisProperties.staticCode(), grandTotal);

            saved.setPaymentCode(dynamicQris);
            saved.setTransactionCode(orderId);
            saved.setExpiredAt(expiryTime);

            qrCodeUrl = baseUrl + "/api/transactions/" + saved.getId() + "/qr-image";
            expiryTimeDisplay = formatExpiry(expiryTime);
        } else {
            QrisChargeResponse qrisResponse = chargerQris(saved, items);
            expiryTime = parseExpiry(qrisResponse.expiryTime());

            saved.setTransactionCode(qrisResponse.transactionId());
            saved.setExpiredAt(expiryTime);

            qrCodeUrl = qrisResponse.qrCodeUrl();
            expiryTimeDisplay = qrisResponse.expiryTime();
        }


        if (rq.phone().isBlank()){
            notificationService.sendPaymentPendingEmail(
                    saved.getEmail(),
                    mapToPaymentPendingDto(items, saved, qrCodeUrl, expiryTime)
            );
        }else {
            var params = List.of(saved.getName(),
                    buildOrderDetailText(items),
                    formatCurrency(saved.getShippingCost()),
                    formatCurrency(grandTotal),
                    formatExpiry(expiryTime),
                    formatDate(saved.getDeliveryDate()));
            notificationOutboxRepository.save(new NotificationOutbox(
                    saved.getPhone(),
                    WhatsAppTemplate.PAYMENT_CREATED_V1,
                    toJson(params),
                    qrCodeUrl
                    )
            );
//            whatsAppService.sendPaymentCreated(
//                    saved.getPhone(),
//                    qrCodeUrl,
//                    saved.getTransactionCode(),
//                    saved.getName(),
//                    grandTotal.toString(),
//                    expiryTime.toString()
//            );
        }

        return transactionMapper.mapToCreateTransactionResponse(
                saved.getId(), orderId, qrCodeUrl, expiryTimeDisplay, grandTotal, saved.getDeliveryDate()
                ,String.format(AppConstant.Success.FETCHED, "Transaction")
        );
    }

    @Override
    public GenericResponse<PageResponse<TransactionResponse>> getAllTransaction(int pageNumber, int pageSize,
                                                                                String sortBy, String sortDirection,
                                                                                String search, Long merchantId,
                                                                                Integer month, Integer year) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortBy)
                : Sort.by(Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Transaction> transactions = transactionRepository.findAll(
                TransactionSpecification.filter(search, merchantId, month, year),
                pageable
        );
        return transactionMapper.mapToPageTransactionResponse(transactions, String.format(AppConstant.Success.FETCHED, "Transaction"));
    }

    @Override
    public byte[] getQrImage(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "Transaction"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Transaction", transactionId)
                ));

        if (transaction.getPaymentCode() == null || transaction.getPaymentCode().isBlank()) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST,
                    AppConstant.Error.TITLE_QR_IMAGE_UNAVAILABLE,
                    AppConstant.Error.MESSAGE_QR_IMAGE_UNAVAILABLE
            );
        }

        return QrCodeImageGenerator.generatePng(transaction.getPaymentCode(), 400);
    }

    @Transactional
    @Override
    public GenericResponse<Void> confirmPayment(Long transactionId, ConfirmPaymentRequest rq, String requesterEmail) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        String.format(AppConstant.Error.TITLE_NOT_FOUND, "Transaction"),
                        String.format(AppConstant.Error.MESSAGE_NOT_FOUND, "Transaction", transactionId)
                ));

        User requester = userService.findByEmail(requesterEmail);
        boolean isManager = requester.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(AppConstant.Role.MANAGER));
        boolean isMerchantKeeper = transaction.getMerchant().getKeeper().getId().equals(requester.getId());

        if (!isManager && !isMerchantKeeper) {
            throw new CustomException(HttpStatus.FORBIDDEN,
                    AppConstant.Error.TITLE_FORBIDDEN,
                    AppConstant.Error.MESSAGE_FORBIDDEN);
        }

        paymentService.confirm(transaction, rq.status(), null, transaction.getTransactionCode());

        return new GenericResponse<>(null, String.format(AppConstant.Success.UPDATED, "Payment status"));
    }


    private PaymentPendingDto mapToPaymentPendingDto(List<TransactionProduct> tp, Transaction tx, String qr, Instant expiryTime){
        List<PaymentPendingDto.OrderItem> items = tp.stream()
                .map(p -> new PaymentPendingDto.OrderItem
                        (p.getProduct().getName(),
                                p.getQuantity(),
                                formatCurrency(p.getSubTotal()))
                ).toList();
        return new PaymentPendingDto(
                tx.getName(),
                tx.getOrderId(),
                qr,
                LocalDateTime.ofInstant(expiryTime, ZoneId.of("Asia/Jakarta")),
                tx.getGrandTotal(),
                tx.getSubTotal(),
                tx.getShippingCost(),
                tx.getAddress(),
                items
        );
    }

    private String buildOrderDetailText(List<TransactionProduct> items) {
        return items.stream()
                .map(item -> String.format("  • %s x%d — %s",
                        item.getProduct().getName(),
                        item.getQuantity(),
                        formatCurrency(item.getSubTotal())))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("-");
    }

    private QrisChargeResponse chargerQris(Transaction transaction, List<TransactionProduct> items) {
        List<QrisChargeRequest.ItemDetail> itemDetails = new ArrayList<>(
                items.stream().map(tp -> new QrisChargeRequest.ItemDetail(
                        tp.getProduct().getId().toString(),
                        tp.getProduct().getName(),
                        tp.getPrice().longValue(),
                        tp.getQuantity()
                )).toList()
        );


        if (transaction.getTaxTotal().compareTo(BigDecimal.ZERO) > 0) {
            itemDetails.add(new QrisChargeRequest.ItemDetail(
                    "TAX",
                    "PPN 11%",
                    transaction.getTaxTotal().longValue(),
                    1
            ));
        }

        if (transaction.getShippingCost().compareTo(BigDecimal.ZERO) > 0) {
            itemDetails.add(new QrisChargeRequest.ItemDetail(
                    "SHIPPING",
                    "Biaya Pengiriman",
                    transaction.getShippingCost().longValue(),
                    1
            ));
        }

        QrisChargeRequest payload = new QrisChargeRequest(
                PaymentMethod.qris.name(),
                new QrisChargeRequest.TransactionDetail(
                        transaction.getOrderId(),
                        transaction.getGrandTotal().longValue()
                ),
                new QrisChargeRequest.CustomerDetail(
                        transaction.getName(),
                        transaction.getEmail(),
                        transaction.getPhone()
                ),
                itemDetails,
                new QrisChargeRequest.CustomExpiry(
                        5,
                        "hour"
                )
        );
        return midtransRestClient.post()
                .uri("/v2/charge")
                .body(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    log.error("Something wrong with requested Midtrans Body   : {}", body);
                    log.error("Midtrans charge request payload for orderId={} : {}",
                            transaction.getOrderId(), writePayloadAsJson(payload));
                    throw new CustomException(HttpStatus.BAD_REQUEST,
                            AppConstant.Error.TITLE_QRIS_CHARGE_FAILED,
                            AppConstant.Error.MESSAGE_QRIS_CHARGE_FAILED);
                })
                .body(QrisChargeResponse.class);
    }

    private String writePayloadAsJson(QrisChargeRequest payload) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            return "<failed to serialize payload: " + e.getMessage() + ">";
        }
    }

    // Build transaction items from Request to TransactionProduct
    private List<TransactionProduct> buildItems(List<ProductItems> products) {
        return products.stream().map(p -> {
            Product product = productService.findProductById(p.productId());
            TransactionProduct tp = new TransactionProduct();
            tp.setProduct(product);
            tp.setQuantity(p.quantity());
            tp.setPrice(p.price());
            tp.setSubTotal(p.price().multiply(BigDecimal.valueOf(p.quantity()))
                    .setScale(2, RoundingMode.HALF_UP));
            return tp;
        }).toList();
    }

    private String toJson(List<String> params){
        try {
            return objectMapper.writeValueAsString(params);
        }catch (Exception ex){
            log.error("Failed to serialize params: {}", ex.getMessage());
            throw new CustomException(HttpStatus.BAD_REQUEST,
                    AppConstant.Error.TITLE_INTERNAL_SERVER_ERROR,
                    AppConstant.Error.MESSAGE_INTERNAL_SERVER_ERROR);
        }
    }
}
