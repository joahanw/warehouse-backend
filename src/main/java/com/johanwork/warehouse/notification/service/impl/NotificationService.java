package com.johanwork.warehouse.notification.service.impl;

import com.johanwork.warehouse.common.config.configProps.MailProperties;
import com.johanwork.warehouse.common.exception.CustomException;
import com.johanwork.warehouse.notification.dto.InvoiceEmailDto;
import com.johanwork.warehouse.notification.dto.PaymentPendingDto;
import com.johanwork.warehouse.notification.service.INotificationService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static com.johanwork.warehouse.common.util.AppUtil.formatCurrency;
import static com.johanwork.warehouse.common.util.AppUtil.formatDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements INotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MailProperties mailProperties;

    @Async("notificationTaskExecutor")
    @Override
    public void sendWelcomeEmail(String toEmail, String customerName) {
        Context ctx = new Context();
        ctx.setVariable("customerName", customerName);
        sendEmail(toEmail, "Welcome to " + mailProperties.name(), "email/welocme", ctx);
    }

    @Async("notificationTaskExecutor")
    @Override
    public void sendPaymentPendingEmail(String toEmail, PaymentPendingDto req) {
        Context ctx = new Context();
        ctx.setVariable("customerName", req.customerName());
        ctx.setVariable("orderId", req.orderId());
        ctx.setVariable("qrCodeUrl", req.qrCodeUrl());
        ctx.setVariable("expiryTime", formatDateTime(req.expiryTime()));
        ctx.setVariable("grandTotal", formatCurrency(req.grandTotal()));
        ctx.setVariable("subTotal", formatCurrency(req.subTotal()));
        ctx.setVariable("shippingCost", formatCurrency(req.shippingCost()));
        ctx.setVariable("address", req.address());
        ctx.setVariable("items", req.items());
        ctx.setVariable("apppName", mailProperties.name());

        sendEmail(toEmail, "Payment Pending - "+req.orderId(), "email/payment-pending", ctx);
    }

    @Async("notificationTaskExecutor")
    @Override
    public void sendInvoiceEmail(String toEmail, InvoiceEmailDto req) {
        Context ctx = new Context();
        ctx.setVariable("invoiceId", req.invoiceId());
        ctx.setVariable("customerName", req.customerName());
        ctx.setVariable("orderId", req.orderId());
        ctx.setVariable("paidAt", formatDateTime(req.paidAt()));
        ctx.setVariable("subTotal", formatCurrency(req.subTotal()));
        ctx.setVariable("taxTotal", formatCurrency(req.taxTotal()));
        ctx.setVariable("grandTotal", formatCurrency(req.grandTotal()));
        ctx.setVariable("address", req.address());
        ctx.setVariable("shippingCost", formatCurrency(req.shippingCost()));
        ctx.setVariable("paymentMethod", req.paymentMethod());
        ctx.setVariable("items", req.items());
        ctx.setVariable("appName", mailProperties.name());

        sendEmail(toEmail, "Invoice from " + mailProperties.name() + " - " + req.orderId(), "email/payment", ctx);
    }

    private void sendEmail(String to, String subject, String template, Context ctx) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailProperties.from(), mailProperties.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(templateEngine.process(template, ctx), true);
            mailSender.send(message);
            log.info("Email sent to:{}, subject:{}",to, subject);
        }catch (Exception e){
            log.error("Failed to send email to: {}, error: {}", to, e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "FAILED SEND EMAIL",
                    "failed to send email to user");
        }
    }

}
