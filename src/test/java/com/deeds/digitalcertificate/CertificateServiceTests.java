package com.deeds.digitalcertificate;

import com.deeds.digitalcertificate.service.ICertificateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * The class Certificate service tests.
 */
@SpringBootTest
public class CertificateServiceTests {

	/**
	 * The Certificate service.
	 */
	@Autowired
	private ICertificateService certificateService;

	/**
	 * Test generate digital deed.
	 */
	@Test
	public void testGenerateDigitalDeed() {
		byte[] certificate = certificateService.generateDigitalDeed();
		Assertions.assertNotNull(certificate);

		/* Printing the document */
		String path = System.getProperty("user.dir");
		String fileDestination = path + "/deed_certificate.pdf";
		try (OutputStream fileOutputStream = new FileOutputStream(fileDestination)) {
			fileOutputStream.write(certificate);
		} catch (Exception ex) {
			throw new RuntimeException("An error occurred!", ex);
		}
	}
}
