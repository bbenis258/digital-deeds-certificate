package com.deeds.digitalcertificate.controller;

import com.deeds.digitalcertificate.service.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

/**
 * The class Certificate controller.
 */
@RestController
public class CertificateController {

	/**
	 * The Certificate service.
	 */
	@Autowired
	private ICertificateService certificateService;

	/**
	 * Generate digital deed response entity.
	 *
	 * @return the response entity
	 */
	@GetMapping("/generate-digital-deed")
	public ResponseEntity<byte[]> generateDigitalDeed() {
		return ResponseEntity.ok(Base64.getEncoder().encode(certificateService.generateDigitalDeed()));
	}
}
