package com.razorpay.integration.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.integration.model.Payment;
import com.razorpay.integration.repository.PaymentRepository;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONObject;

@Service
public class PaymentService {

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private PaymentRepository paymentRepository;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    // Create Razorpay order
    public String createOrder(int amount) throws RazorpayException {
        JSONObject options = new JSONObject();
        options.put("amount", amount * 100); // INR to paise
        options.put("currency", "INR");
        options.put("receipt", "txn_123456");

        Order order = razorpayClient.orders.create(options);
        return order.toString();
    }

    // Verify Razorpay payment signature
    public boolean verifyPayment(String orderId, String paymentId, String razorpaySignature) throws Exception {
        String data = orderId + "|" + paymentId;

        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secretKey);

        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // Convert byte[] to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        String generatedSignature = hexString.toString();

        System.out.println("Generated: " + generatedSignature);
        System.out.println("Received : " + razorpaySignature);

        return generatedSignature.equals(razorpaySignature);
    }

    // Verify payment and save to DB
    public boolean verifyAndSavePayment(String orderId, String paymentId, String razorpaySignature, Integer amount) throws Exception {
        boolean isValid = verifyPayment(orderId, paymentId, razorpaySignature);

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentId(paymentId);
        payment.setSignature(razorpaySignature);
        payment.setAmount(amount);
        payment.setStatus(isValid ? "SUCCESS" : "FAILED");

        paymentRepository.save(payment); // Works perfectly with Lombok & JPA

        return isValid;
    }
}