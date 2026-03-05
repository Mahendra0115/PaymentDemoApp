package com.razorpay.integration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.razorpay.RazorpayException;
import com.razorpay.integration.service.PaymentService;

@RestController
@RequestMapping("/payment")
public class PaymentController {

	@Autowired
	private PaymentService paymentService ;
	
	@PostMapping("/create-order")
	public ResponseEntity<String> createOrder(@RequestParam int amount)throws RazorpayException {
		
		String order = paymentService.createOrder(amount);
		return ResponseEntity.ok(order);
		
	}
	
	
	//  Payment Verification Endpoint
	@PostMapping("/verify")
	public ResponseEntity<String> verifyPayment(
	        @RequestParam String razorpay_order_id,
	        @RequestParam String razorpay_payment_id,
	        @RequestParam String razorpay_signature,
	        @RequestParam Integer amount) throws Exception {

	    boolean isValid = paymentService.verifyAndSavePayment(
	            razorpay_order_id,
	            razorpay_payment_id,
	            razorpay_signature,
	            amount
	    );

	    if (isValid) {
	        return ResponseEntity.ok("Payment Verified & Saved ");
	    } else {
	        return ResponseEntity.badRequest().body("Payment Failed ");
	    }
	}
}
