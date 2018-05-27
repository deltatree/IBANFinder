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

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;

import nl.garvelink.iban.IBAN;
import nl.garvelink.iban.IBANFields;

public class Main {

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

	private static void analyse(String uri) throws IOException {
		Stream<String> distinct = stream(uri).flatMap(line -> MatcherStream.find(PATTERN_IBAN, line))
				.map(result -> result.replaceAll("\\s{1,}", "")).distinct();
		distinct.map(iban -> validateIBAN(iban)).filter(Objects::nonNull).forEach(iban -> doit(iban));
	}

	private static Object doit(IBAN iban) {
		System.out.println(IBANFields.getBankIdentifier(iban));
		System.out.println(iban);
		return null;
	}

	private static IBAN validateIBAN(String iban) {
		try {
			return IBAN.parse(iban);
		} catch (Exception e) {
			return null;
		}
	}

	private static Stream<String> stream(String uri) throws IOException {
		InputStream is = Request.Get(uri).execute().returnContent().asStream();
		Charset cs = Charset.forName("UTF-8");
		BufferedReader br = new BufferedReader(new InputStreamReader(is, cs));
		return br.lines();
	}

}
