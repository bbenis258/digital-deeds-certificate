package com.deeds.digitalcertificate.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class DeedInfoDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private int id;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String pDescr;
	private String township;
	private String titleNumber;
	private String dateOfGrant;
	private String transferNumber;
	private String dateOfTransfer;
	private String folioNumber;
	private String district;
	private String extent;
	private String unitOfExt;
	private String nameOfGrant;
	private String endorsement;
	private String purchaseValue;
	private String docID;
	private String tshipCode;
	private String propertyDescr;
	private String docStatus;
	private String status;
	private String districtCode;
	private String gNatID;
	private String gDOB;
	private String gender;
	private String updatedDate;
	private String rFlag;
}
