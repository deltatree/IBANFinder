package de.deltatree.tools.iban.finder;

import java.util.List;

import lombok.Data;

@Data
public class OpenIBANValidationResult {
	// {
	// "valid": true,
	// "messages": [
	// "Bank code valid: 20120700"
	// ],
	// "iban": "DE75201207003100124444",
	// "bankData": {
	// "bankCode": "20120700",
	// "name": "Hanseatic Bank",
	// "zip": "22177",
	// "city": "Hamburg",
	// "bic": "HSTBDEHHXXX"
	// },
	// "checkResults": {
	// "bankCode": true
	// }
	// }

	boolean valid;
	List<String> messages;
	String iban;

	OpenIBANValidationResultBankData bankData;

}
