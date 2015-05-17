package app.report;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestStatus.OK;

import org.apache.commons.codec.Charsets;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestHandler;
import org.elasticsearch.rest.RestRequest;
import org.json.JSONObject;

public class ESReportPluginRestHandler implements RestHandler {
	private Client client; // client to save for later

	@Inject
	public ESReportPluginRestHandler(Settings settings, Client client, RestController restController) {
		this.client = client;
		restController.registerHandler(POST, "/_river/report/now", this);
	}

	public void handleRequest(final RestRequest request, final RestChannel channel) {

		String inputStr = new String(request.content().toBytes(), Charsets.UTF_8);

		JSONObject input = null;
		if (!request.method().equals(GET)) {
			input = new JSONObject(inputStr);
		}

		System.out.println("Path:" + request.path());

		if (request.path().equals("/_river/report/now")) {
			ESReport esReport = new ESReport(client);
			esReport.process(inputStr);
			channel.sendResponse(new BytesRestResponse(OK, "{status:finished}"));
		}
	}
}
