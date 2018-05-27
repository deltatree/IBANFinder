package de.deltatree.tools.iban.finder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.hc.client5.http.fluent.Request;
import org.iban4j.IbanUtil;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;

public class Main {

	private final static RestTemplate REST = new RestTemplate();
	private final static String URL_TEMPLATE = "https://openiban.com/validate/{ibanString}?getBIC=true&validateBankCode=true";

	private final static Pattern PATTERN_IBAN = Pattern.compile(
			"[a-zA-Z]{2}[0-9]{2}\\s?[a-zA-Z0-9]{4}\\s?[0-9]{4}\\s?[0-9]{3}([a-zA-Z0-9]\\s?[a-zA-Z0-9]{0,4}\\s?[a-zA-Z0-9]{0,4}\\s?[a-zA-Z0-9]{0,4}\\s?[a-zA-Z0-9]{0,3})?");

	public static void main(String[] args) throws IOException {

		Customsearch cs = new Customsearch.Builder(new NetHttpTransport(), new JacksonFactory(), null)
				.setApplicationName("ibanfinder").build();
		Customsearch.Cse.List list = cs.cse().list("iban otto.de");

		// ID
		list.setKey("XXX");
		list.setCx("YYY");

		Result result = list.execute().getItems().get(0);

		System.out.println(result.getLink());
		analyse(result.getLink());

	}

	private static OpenIBANValidationResult validateOpenIBAN(String ibanString) {
		try {
			OpenIBANValidationResult oivr = REST.getForObject(URL_TEMPLATE, OpenIBANValidationResult.class, ibanString);
			if (oivr.isValid()) {
				return oivr;
			}
		} catch (Exception ignore) {
			/* ignore */
		}
		return null;
	}

	private static void analyse(String uri) throws IOException {
		stream(uri).flatMap(line -> MatcherStream.find(PATTERN_IBAN, line))
				.map(result -> result.replaceAll("\\s{1,}", "")).distinct().filter(iban -> validateIBAN(iban))
				.map(iban -> validateOpenIBAN(iban)).filter(Objects::nonNull).forEach(System.out::println);
	}

	private static boolean validateIBAN(String iban) {
		try {
			IbanUtil.validate(iban);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private static Stream<String> stream(String uri) throws IOException {
		InputStream is = Request.Get(uri).execute().returnContent().asStream();
		Charset cs = Charset.forName("UTF-8");
		BufferedReader br = new BufferedReader(new InputStreamReader(is, cs));
		return br.lines();
	}

}
