package com.deeds.digitalcertificate.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * The class Background page event.
 */
public class BackgroundPageEvent extends PdfPageEventHelper {

	/**
	 * The Image.
	 */
	private Image image;

	/**
	 * Instantiates a new Background page event.
	 *
	 * @param image the image
	 */
	public BackgroundPageEvent(Image image) {
		this.image = image;
	}

	/**
	 * On end page.
	 *
	 * @param writer   the writer
	 * @param document the document
	 */
	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		try {
			writer.getDirectContentUnder().addImage(image);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
}
