package com.deeds.digitalcertificate.service;

import com.deeds.digitalcertificate.dto.DeedInfoDto;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.qrcode.EncodeHintType;
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The class Certificate service.
 */
@Service
public class CertificateService implements ICertificateService {


	/**
	 * The constant BLACK.
	 */
	private final static BaseColor BLACK = new BaseColor(0, 0, 0, 255);

	/**
	 * The constant RED.
	 */
	private final static BaseColor RED = new BaseColor(244, 0, 42, 255);

	/**
	 * The constant ORANGE.
	 */
	private final static BaseColor ORANGE = new BaseColor(255, 196, 0, 255);

	/**
	 * The constant GREEN.
	 */
	private final static BaseColor GREEN = new BaseColor(0, 136, 57, 255);

	/**
	 * The constant WHITE.
	 */
	private final static BaseColor WHITE = new BaseColor(255, 255, 255, 255);

	/**
	 * The constant BASKERVILE_SEMIBOLD.
	 */
	public static final String BASKERVILE_SEMIBOLD = new ClassPathResource("fonts/BaskervilleSemiBold.ttf").getPath();

	public static final String HELVETICANEUE_ROMAN = new ClassPathResource("fonts/HelveticaNeue-Roman.otf").getPath();

	/**
	 * Gets deed information.
	 *
	 * @param docId the doc id
	 * @return the deed information
	 */
	private DeedInfoDto getDeedInformation(String docId) {
		try {
			WebClient deedInfoClient = WebClient
					.builder()
					.baseUrl("https://deeds-api-2-staging.dokuma.rw")
					.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.build();

			WebClient.RequestHeadersSpec<?> deedInfoRequest = deedInfoClient
					.method(HttpMethod.GET)
					.uri("/api/lands/" + docId + "/document");

			return deedInfoRequest.retrieve().bodyToMono(DeedInfoDto.class).block();
		} catch (Exception ex) {
			throw new RuntimeException("An error occurred while fetching the land information.", ex);
		}
	}

	/**
	 * Generate digital deed byte [ ].
	 *
	 * @param docId the doc id
	 * @return the byte [ ]
	 */
	@Override
	public byte[] generateDigitalDeed(String docId) {
		DeedInfoDto deedInfoDto = this.getDeedInformation(docId);
		Rectangle rectangle = PageSize.A3.rotate();
		Document document = new Document(rectangle);
		ByteArrayOutputStream outputStream;
		try {
			outputStream = new ByteArrayOutputStream();
		} catch (Exception ex) {
			throw new RuntimeException("An error occurred while creating the output stream.", ex);
		}
		PdfWriter writer;
		try {
			writer = PdfWriter.getInstance(document, outputStream);
		} catch (Exception ex) {
			throw new RuntimeException("An error occurred while creating the PdfWriter object.", ex);
		}

		try {
			Resource backgroundResource = new ClassPathResource("images/Background.png");
			Image backgroundImage = Image.getInstance(backgroundResource.getURL());
			backgroundImage.setAbsolutePosition(-55, -128);
			writer.setPageEvent(new BackgroundPageEvent(backgroundImage));
			Image backgroundImageTwo = Image.getInstance(backgroundResource.getURL());
			backgroundImageTwo.setAbsolutePosition(595, -128);
			writer.setPageEvent(new BackgroundPageEvent(backgroundImageTwo));
		} catch (Exception ex) {
			throw new RuntimeException("An error occurred while adding the page background.", ex);
		}

		document.open();

		try {

			/*
			 * First page
			 */

			PdfPTable container = new PdfPTable(2);
			container.setWidthPercentage(100);

			PdfPTable leftContainer = new PdfPTable(1);
			leftContainer.setWidthPercentage(100);

			Resource sealHeadResource = new ClassPathResource("images/Coat_of_arms_of_Zimbabwe_head.png");
			Image sealHeadImage = Image.getInstance(sealHeadResource.getURL());
			sealHeadImage.scaleAbsolute(300, 300);

			PdfPCell sealHead = new PdfPCell(sealHeadImage);
			sealHead.setHorizontalAlignment(Element.ALIGN_CENTER);
			sealHead.setPaddingTop(170);
			sealHead.setBorder(Rectangle.NO_BORDER);
			leftContainer.addCell(sealHead);

			Map<EncodeHintType, Object> qrParam = new HashMap<EncodeHintType, Object>();
			qrParam.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
			qrParam.put(EncodeHintType.CHARACTER_SET, "UTF-8");

			BarcodeQRCode qrCode = new BarcodeQRCode("https://www.google.com", 200, 200, qrParam);
			qrCode.createAwtImage(Color.black, Color.WHITE);

			PdfPCell qrCell = new PdfPCell(qrCode.getImage());
			qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			qrCell.setPaddingTop(20);
			qrCell.setBorder(Rectangle.NO_BORDER);
			leftContainer.addCell(qrCell);

			PdfPCell leftPart = new PdfPCell(leftContainer);
			leftPart.setBorderWidth(1.5f);
			leftPart.setBorderWidthRight(0);
			leftPart.setBorderColor(GREEN);

			PdfPTable rightContainer = new PdfPTable(new float[]{1f, 4f});
			rightContainer.setWidthPercentage(100);

			PdfPCell flagCell = new PdfPCell(this.generateFlag());
			rightContainer.addCell(flagCell);

			Resource coatArmResource = new ClassPathResource("images/Coat_of_arms_of_Zimbabwe.png");
			Image coatArms = Image.getInstance(coatArmResource.getURL());
			coatArms.scaleAbsolute(120, 120);

			PdfPTable titleContent = new PdfPTable(1);
			titleContent.setWidthPercentage(100);

			PdfPCell seal = new PdfPCell(coatArms);
			seal.setHorizontalAlignment(Element.ALIGN_CENTER);
			seal.setBorder(Rectangle.NO_BORDER);

			PdfPCell sealHeader = new PdfPCell(new Paragraph(" "));
			sealHeader.setFixedHeight(140);
			sealHeader.setBorder(Rectangle.NO_BORDER);
			titleContent.addCell(sealHeader);
			titleContent.addCell(seal);

			BaseFont institutionBaseFont = BaseFont.createFont(HELVETICANEUE_ROMAN, BaseFont.WINANSI, BaseFont.EMBEDDED);
			Font institutionFont = new Font(institutionBaseFont, 20);
			Paragraph institutionOne = new Paragraph("REPUBLIC OF ZIMBABWE", institutionFont);
			institutionOne.setAlignment(Element.ALIGN_CENTER);
			PdfPCell titleInstitutionOne = new PdfPCell(institutionOne);
			titleInstitutionOne.setHorizontalAlignment(Element.ALIGN_CENTER);
			titleInstitutionOne.setPaddingTop(20);
			titleInstitutionOne.setBorder(Rectangle.NO_BORDER);
			titleContent.addCell(titleInstitutionOne);
			Paragraph institutionTwo = new Paragraph("OFFICE OF THE PRESIDENT", institutionFont);
			institutionTwo.setAlignment(Element.ALIGN_CENTER);
			PdfPCell titleInstitutionTwo = new PdfPCell(institutionTwo);
			titleInstitutionTwo.setHorizontalAlignment(Element.ALIGN_CENTER);
			titleInstitutionTwo.setPaddingTop(10);
			titleInstitutionTwo.setBorder(Rectangle.NO_BORDER);
			titleContent.addCell(titleInstitutionTwo);
			Paragraph institutionThree = new Paragraph("AND CABINET", institutionFont);
			institutionThree.setAlignment(Element.ALIGN_CENTER);
			PdfPCell titleInstitutionThree = new PdfPCell(institutionThree);
			titleInstitutionThree.setHorizontalAlignment(Element.ALIGN_CENTER);
			titleInstitutionThree.setPaddingTop(10);
			titleInstitutionThree.setBorder(Rectangle.NO_BORDER);
			titleContent.addCell(titleInstitutionThree);

			BaseFont docNameBaseFont = BaseFont.createFont(BASKERVILE_SEMIBOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);
			Font docNameFont = new Font(docNameBaseFont, 40, Font.BOLD);
			Paragraph docName = new Paragraph("Deed of Grant", docNameFont);
			docName.setAlignment(Element.ALIGN_CENTER);
			PdfPCell titleDocName = new PdfPCell(docName);
			titleDocName.setHorizontalAlignment(Element.ALIGN_CENTER);
			titleDocName.setPaddingTop(60);
			titleDocName.setBorder(Rectangle.NO_BORDER);
			titleContent.addCell(titleDocName);

			Font docOwnerFont = new Font(institutionBaseFont, 14);
			Font docOwnerContentFont = new Font(institutionBaseFont, 14, Font.BOLD);
			Paragraph docOwnerTitle = new Paragraph("In favour of: ", docOwnerFont);
			Paragraph docOwnerContent = new Paragraph(deedInfoDto.getNameOfGrant(), docOwnerContentFont);
			Phrase docOwnerPhrase = new Phrase();
			docOwnerPhrase.add(docOwnerTitle);
			docOwnerPhrase.add(docOwnerContent);
			PdfPCell titleDocOwner = new PdfPCell(docOwnerPhrase);
			titleDocOwner.setPaddingTop(200);
			titleDocOwner.setPaddingLeft(30);
			titleDocOwner.setBorder(Rectangle.NO_BORDER);
			titleContent.addCell(titleDocOwner);
			Paragraph docIdentifierTitle = new Paragraph("Unique Plot Identifier Number: ", docOwnerFont);
			Paragraph docIdentifierContent = new Paragraph(deedInfoDto.getTitleNumber(), docOwnerContentFont);
			Phrase docIdentifierPhrase = new Phrase();
			docIdentifierPhrase.add(docIdentifierTitle);
			docIdentifierPhrase.add(docIdentifierContent);
			PdfPCell titleDocIdentifier = new PdfPCell(docIdentifierPhrase);
			titleDocIdentifier.setPaddingTop(10);
			titleDocIdentifier.setPaddingLeft(30);
			titleDocIdentifier.setBorder(Rectangle.NO_BORDER);
			titleContent.addCell(titleDocIdentifier);

			PdfPCell titleCell = new PdfPCell(titleContent);
			titleCell.setBorder(Rectangle.NO_BORDER);
			rightContainer.addCell(titleCell);

			PdfPCell rightPart = new PdfPCell(rightContainer);
			rightPart.setBorderWidth(1.5f);
			rightPart.setBorderWidthLeft(0);
			rightPart.setBorderColor(GREEN);
			rightPart.setPaddingLeft(10);

			container.addCell(leftPart);
			container.addCell(rightPart);

			document.add(container);

			/*
			 * Second page
			 */

			document.newPage();

			PdfPTable containerPageTwo = new PdfPTable(2);
			containerPageTwo.setWidthPercentage(100);

			PdfPTable backContent = new PdfPTable(1);
			backContent.setWidthPercentage(100);

			Font backContentFont = new Font(institutionBaseFont, 12);
			Font backContentBoldFont = new Font(institutionBaseFont, 12, Font.BOLD);
			Paragraph backPar1 = new Paragraph("By the President of the Republic of Zimbabwe, in terms of Cabinet Minute No. 316 of 1985.", backContentFont);
			PdfPCell backCell1 = new PdfPCell(backPar1);
			backCell1.setBorder(Rectangle.NO_BORDER);
			backContent.addCell(backCell1);
			Paragraph backPar2_1 = new Paragraph("I do hereby grant unto ", backContentFont);
			Paragraph backPar2_2 = new Paragraph(deedInfoDto.getNameOfGrant(), backContentBoldFont);
			Paragraph backPar2_3 = new Paragraph("\nHereinafter referred to as the owner, a piece of land measuring ", backContentFont);
			Paragraph backPar2_4 = new Paragraph(deedInfoDto.getExtent() + " " + deedInfoDto.getUnitOfExt(), backContentBoldFont);
			Paragraph backPar2_5 = new Paragraph(".", backContentFont);
			Phrase backParPhrase2 = new Phrase();
			backParPhrase2.add(backPar2_1);
			backParPhrase2.add(backPar2_2);
			backParPhrase2.add(backPar2_3);
			backParPhrase2.add(backPar2_4);
			backParPhrase2.add(backPar2_5);
			PdfPCell backCell2 = new PdfPCell(backParPhrase2);
			backCell2.setPaddingTop(12);
			backCell2.setLeading(1, 1.3f);
			backCell2.setBorder(Rectangle.NO_BORDER);
			backContent.addCell(backCell2);
			Paragraph backPar3_1 = new Paragraph("Called ", backContentFont);
			Paragraph backPar3_2 = new Paragraph(deedInfoDto.getPropertyDescr(), backContentBoldFont);
			Phrase backParPhrase3 = new Phrase();
			backParPhrase3.add(backPar3_1);
			backParPhrase3.add(backPar3_2);
			PdfPCell backCell3 = new PdfPCell(backParPhrase3);
			backCell3.setPaddingTop(12);
			backCell3.setLeading(1, 1.3f);
			backCell3.setBorder(Rectangle.NO_BORDER);
			backContent.addCell(backCell3);
			Paragraph backPar4 = new Paragraph("Hereinafter referred to as the said land", backContentFont);
			PdfPCell backCell4 = new PdfPCell(backPar4);
			backCell4.setPaddingTop(12);
			backCell4.setLeading(1, 1.3f);
			backCell4.setBorder(Rectangle.NO_BORDER);
			backContent.addCell(backCell4);
			Paragraph backPar5_1 = new Paragraph("In the district of ", backContentFont);
			Paragraph backPar5_2 = new Paragraph(deedInfoDto.getDistrict(), backContentBoldFont);
			Phrase backParPhrase5 = new Phrase();
			backParPhrase5.add(backPar5_1);
			backParPhrase5.add(backPar5_2);
			PdfPCell backCell5 = new PdfPCell(backParPhrase5);
			backCell5.setPaddingTop(12);
			backCell5.setLeading(1, 1.3f);
			backCell5.setBorder(Rectangle.NO_BORDER);
			backContent.addCell(backCell5);
			Paragraph backPar6_1 = new Paragraph("And represented and described on General Plan ", backContentFont);
			Paragraph backPar6_2 = new Paragraph(String.valueOf(new Random().nextInt()), backContentBoldFont);
			Paragraph backPar6_3 = new Paragraph(" dated ", backContentFont);
			Paragraph backPar6_4 = new Paragraph(LocalDate.now().getDayOfMonth() + this.getDayOfMonthSuffix(LocalDate.now().getDayOfMonth()) + " " +
					LocalDate.now().getMonth().toString() + ", " + LocalDate.now().getYear(), backContentBoldFont);
			Paragraph backPar6_5 = new Paragraph(" filed in the office of the Surveyor-General, upon the following conditions,viz,:", backContentFont);
			Phrase backParPhrase6 = new Phrase();
			backParPhrase6.add(backPar6_1);
			backParPhrase6.add(backPar6_2);
			backParPhrase6.add(backPar6_3);
			backParPhrase6.add(backPar6_4);
			backParPhrase6.add(backPar6_5);
			PdfPCell backCell6 = new PdfPCell(backParPhrase6);
			backCell6.setPaddingTop(12);
			backCell6.setLeading(1, 1.3f);
			backCell6.setBorder(Rectangle.NO_BORDER);
			backContent.addCell(backCell6);
			Paragraph backPar7 = new Paragraph("The President of the Republic of Zimbabwe authority in whose area the said land is situated shall at all times " +
					"have the right and power, free of charge, to erect or lay and work pipelines, electric lines, sewers, drains, poles and standard upon, over or " +
					"under the said land with the further right and power to enter upon the said land at all reasonable times free of charge for the purpose of inspecting, " +
					"repairing, maintaining, replacing or altering such works in connection therewith.", backContentFont);
			PdfPCell backCell7 = new PdfPCell(backPar7);
			backCell7.setPaddingTop(12);
			backCell7.setLeading(1, 1.3f);
			backCell7.setBorder(Rectangle.NO_BORDER);
			backContent.addCell(backCell7);
			Paragraph backPar8 = new Paragraph("GIVEN...", backContentFont);
			PdfPCell backCell8 = new PdfPCell(backPar8);
			backCell8.setPaddingTop(30);
			backCell8.setLeading(1, 1.3f);
			backCell8.setBorder(Rectangle.NO_BORDER);
			backContent.addCell(backCell8);
			LocalDate grantDate = LocalDate.parse(deedInfoDto.getDateOfGrant(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
			Paragraph backPar9_1 = new Paragraph("Given under my hand at Harare this ", backContentFont);
			Paragraph backPar9_2 = new Paragraph(grantDate.getDayOfMonth() + this.getDayOfMonthSuffix(grantDate.getDayOfMonth()), backContentBoldFont);
			Paragraph backPar9_3 = new Paragraph(" day of ", backContentFont);
			Paragraph backPar9_4 = new Paragraph(grantDate.getMonth().toString(), backContentBoldFont);
			Paragraph backPar9_5 = new Paragraph(" " + grantDate.getYear(), backContentBoldFont);
			Paragraph backPar9_6 = new Paragraph(".", backContentFont);
			Phrase backParPhrase9 = new Phrase();
			backParPhrase9.add(backPar9_1);
			backParPhrase9.add(backPar9_2);
			backParPhrase9.add(backPar9_3);
			backParPhrase9.add(backPar9_4);
			backParPhrase9.add(backPar9_5);
			backParPhrase9.add(backPar9_6);
			PdfPCell backCell9 = new PdfPCell(backParPhrase9);
			backCell9.setPaddingTop(45);
			backCell9.setLeading(1, 1.3f);
			backCell9.setBorder(Rectangle.NO_BORDER);
			backContent.addCell(backCell9);
			Paragraph backPar10 = new Paragraph("PRESIDENT OF THE REPUBLIC OF ZIMBABWE,", backContentFont);
			PdfPCell backCell10 = new PdfPCell(backPar10);
			backCell10.setPaddingTop(40);
			backCell10.setPaddingRight(30);
			backCell10.setLeading(1, 1.3f);
			backCell10.setHorizontalAlignment(Element.ALIGN_RIGHT);
			backCell10.setBorder(Rectangle.NO_BORDER);
			backContent.addCell(backCell10);
			Paragraph backPar11 = new Paragraph("Terms of Statutory Instrument 17 of 1986.", backContentFont);
			PdfPCell backCell11 = new PdfPCell(backPar11);
			backCell11.setPaddingTop(40);
			backCell11.setPaddingRight(30);
			backCell11.setLeading(1, 1.3f);
			backCell11.setHorizontalAlignment(Element.ALIGN_RIGHT);
			backCell11.setBorder(Rectangle.NO_BORDER);
			backContent.addCell(backCell11);

			PdfPCell leftPartPageTwo = new PdfPCell(backContent);
			leftPartPageTwo.setBorderWidth(1.5f);
			leftPartPageTwo.setBorderWidthRight(0);
			leftPartPageTwo.setBorderColor(GREEN);
			leftPartPageTwo.setPaddingLeft(30);
			leftPartPageTwo.setPaddingTop(15);
			leftPartPageTwo.setPaddingBottom(10);

			PdfPTable mapTable = new PdfPTable(3);
			mapTable.setWidthPercentage(100);

			PdfPCell mainMapCell = new PdfPCell(new Paragraph(" "));
			mainMapCell.setColspan(3);
			mainMapCell.setFixedHeight(470);
			mainMapCell.setBorderWidth(2);
			mainMapCell.setBorderColor(GREEN);
			mainMapCell.setBackgroundColor(WHITE);
			mapTable.addCell(mainMapCell);

			PdfPCell middleEmpty1 = new PdfPCell(new Paragraph(" "));
			middleEmpty1.setFixedHeight(30);
			middleEmpty1.setBorderWidth(2);
			middleEmpty1.setBorderColor(GREEN);
			middleEmpty1.setBackgroundColor(WHITE);
			mapTable.addCell(middleEmpty1);

			PdfPCell middleEmpty2 = new PdfPCell(new Paragraph(" "));
			middleEmpty2.setFixedHeight(30);
			middleEmpty2.setBorderWidth(2);
			middleEmpty2.setBorderColor(GREEN);
			middleEmpty2.setBackgroundColor(WHITE);
			mapTable.addCell(middleEmpty2);

			PdfPCell middleEmpty3 = new PdfPCell(new Paragraph(" "));
			middleEmpty3.setFixedHeight(30);
			middleEmpty3.setBorderWidth(2);
			middleEmpty3.setBorderColor(GREEN);
			middleEmpty3.setBackgroundColor(WHITE);
			mapTable.addCell(middleEmpty3);

			PdfPCell miniMap1 = new PdfPCell(new Paragraph(" "));
			miniMap1.setFixedHeight(180);
			miniMap1.setBorderWidth(2);
			miniMap1.setBorderColor(GREEN);
			miniMap1.setBackgroundColor(WHITE);
			mapTable.addCell(miniMap1);

			PdfPCell miniMap2 = new PdfPCell(new Paragraph(" "));
			miniMap2.setFixedHeight(180);
			miniMap2.setBorderWidth(2);
			miniMap2.setBorderColor(GREEN);
			miniMap2.setBackgroundColor(WHITE);
			mapTable.addCell(miniMap2);

			PdfPCell miniMap3 = new PdfPCell(new Paragraph(" "));
			miniMap3.setFixedHeight(180);
			miniMap3.setBorderWidth(2);
			miniMap3.setBorderColor(GREEN);
			miniMap3.setBackgroundColor(WHITE);
			mapTable.addCell(miniMap3);

			PdfPCell rightPartPageTwo = new PdfPCell(mapTable);
			rightPartPageTwo.setBorderWidth(1.5f);
			rightPartPageTwo.setBorderWidthLeft(0);
			rightPartPageTwo.setBorderColor(GREEN);
			rightPartPageTwo.setPaddingTop(15);
			rightPartPageTwo.setPaddingRight(20);
			rightPartPageTwo.setPaddingBottom(10);

			containerPageTwo.addCell(leftPartPageTwo);
			containerPageTwo.addCell(rightPartPageTwo);

			document.add(containerPageTwo);

			/*
			 * Flag footer
			 */

			PdfPTable flagFooter = new PdfPTable(1);
			flagFooter.setWidthPercentage(100);

			PdfPTable flags = new PdfPTable(1);
			flags.setWidthPercentage(100);
			/* black color of the flag */
			PdfPCell blackShade = new PdfPCell(new Paragraph(" "));
			blackShade.setFixedHeight(15);
			blackShade.setBackgroundColor(BLACK);
			/* red color of the flag */
			PdfPCell redShade = new PdfPCell(new Paragraph(" "));
			redShade.setFixedHeight(15);
			redShade.setBackgroundColor(RED);
			/* orange color of the flag */
			PdfPCell orangeShade = new PdfPCell(new Paragraph(" "));
			orangeShade.setFixedHeight(15);
			orangeShade.setBackgroundColor(ORANGE);
			/* green color of the flag */
			PdfPCell greenShade = new PdfPCell(new Paragraph(" "));
			greenShade.setFixedHeight(15);
			greenShade.setBackgroundColor(GREEN);

			flags.addCell(blackShade);
			flags.addCell(redShade);
			flags.addCell(orangeShade);
			flags.addCell(greenShade);

			PdfPCell flagFooterCell = new PdfPCell(flags);
			flagFooterCell.setBorder(Rectangle.NO_BORDER);
			flagFooter.addCell(flagFooterCell);

			document.add(flagFooter);
		} catch (Exception ex) {
			throw new RuntimeException("An error occurred while generating the certificate.", ex);
		}

		document.close();

		return outputStream.toByteArray();
	}

	/**
	 * Generate flag pdf p table.
	 *
	 * @return the pdf p table
	 */
	private PdfPTable generateFlag() {
		PdfPTable flags = new PdfPTable(1);
		flags.setWidthPercentage(100);
		/* black color of the flag */
		PdfPCell blackShade = new PdfPCell(new Paragraph(" "));
		blackShade.setFixedHeight(40);
		blackShade.setBackgroundColor(BLACK);
		/* red color of the flag */
		PdfPCell redShade = new PdfPCell(new Paragraph(" "));
		redShade.setFixedHeight(40);
		redShade.setBackgroundColor(RED);
		/* orange color of the flag */
		PdfPCell orangeShade = new PdfPCell(new Paragraph(" "));
		orangeShade.setFixedHeight(40);
		orangeShade.setBackgroundColor(ORANGE);
		/* green color of the flag */
		PdfPCell greenShade = new PdfPCell(new Paragraph(" "));
		greenShade.setFixedHeight(40);
		greenShade.setBackgroundColor(GREEN);
		/* white color of the flag */
		PdfPCell whiteShade = new PdfPCell(new Paragraph(" "));
		whiteShade.setFixedHeight(40);
		whiteShade.setBackgroundColor(WHITE);
		for (int i = 0; i < 3; i++) {
			flags.addCell(blackShade);
			flags.addCell(redShade);
			flags.addCell(orangeShade);
			flags.addCell(greenShade);
			flags.addCell(whiteShade);
		}
		flags.addCell(blackShade);
		flags.addCell(redShade);
		flags.addCell(orangeShade);
		flags.addCell(greenShade);
		whiteShade.setFixedHeight(10);
		flags.addCell(whiteShade);
		return flags;
	}

	/**
	 * Gets day of month suffix.
	 *
	 * @param n the n
	 * @return the day of month suffix
	 */
	private String getDayOfMonthSuffix(final int n) {
		if (!(n >= 1 && n <= 31)) {
			throw new RuntimeException("Illegal dat of the month: " + n);
		}
		if (n >= 11 && n <= 13) {
			return "th";
		}
		switch (n % 10) {
			case 1:
				return "st";
			case 2:
				return "nd";
			case 3:
				return "rd";
			default:
				return "th";
		}
	}
}
