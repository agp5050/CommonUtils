import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class HttpParamsBuilder {
	private static final Logger logger = Logger.getLogger(ParamsBuilder.class);

	public static String convert(Map<String, ? extends Object> params) {
		return convert(params, StandardCharsets.UTF_8);
	}

	public static String convert(Map<String, ? extends Object> params,
			Charset charset) {
		StringBuilder builder = new StringBuilder();
		for (Entry<String, ? extends Object> entry : params.entrySet()) {
			builder.append("&")
					.append(encode(entry.getKey(), charset))
					.append("=")
					.append(encode(entry.getValue() == null ? "" : entry
							.getValue().toString(), charset));
		}
		return builder.toString();
	}

	private static String encode(String value, Charset charset) {
		try {
			return URLEncoder.encode(value, charset.name());
		} catch (UnsupportedEncodingException x) {
			logger.error(x.getMessage(), x);
		}
		return "";
	}

	public static String params2string(Map<String, String[]> params) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		if (params != null && !params.isEmpty()) {
			for (Entry<String, String[]> entry : params.entrySet()) {
				sb.append(
						entry.getKey() + "="
								+ StringUtils.join(entry.getValue(), "|"))
						.append(";");
			}
		}
		sb.append("}");
		return sb.toString();
	}
}
