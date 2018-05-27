package de.deltatree.tools.iban.finder;

import lombok.Data;

@Data
public class OpenIBANValidationResultBankData {
	String bankCode;
	String name;
	String zip;
	String city;
	String bic;
}
