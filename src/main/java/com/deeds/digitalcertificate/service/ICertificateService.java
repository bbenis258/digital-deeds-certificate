package com.deeds.digitalcertificate.service;

/**
 * The interface Certificate service.
 */
public interface ICertificateService {

	/**
	 * Generate digital deed byte [ ].
	 *
	 * @param docId the doc id
	 * @return the byte [ ]
	 */
	byte[] generateDigitalDeed(String docId);
}
