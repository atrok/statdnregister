package com.genesys.x.statdnregister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesyslab.platform.reporting.protocol.statserver.DnActions;
import com.genesyslab.platform.reporting.protocol.statserver.StatisticObject;
import com.genesyslab.platform.reporting.protocol.statserver.events.EventInfo;
import com.genesyslab.platform.reporting.protocol.statserver.requests.RequestOpenStatisticEx;

public class RegistrationResult {
	private static final Logger logger = LogManager.getLogger();

	private HashMap<Integer, RequestOpenStatisticEx> statIDList = new HashMap<Integer, RequestOpenStatisticEx>();

	private HashMap<String, HashMap> statsrv = new HashMap<String, HashMap>();

	private ArrayList processed = new ArrayList();

	private String appname = "";

	public void addRequest(RequestOpenStatisticEx req) {

		statIDList.put(req.getReferenceId(), req);

	}

	public RegistrationResult() {
	}

	public RegistrationResult(String appname) {
		this.appname = appname;
	}

	public void add(EventInfo ev) {
		try {
			// if(status instanceof DnStatus){
			if (ev != null) {
				// System.out.printf("Event reference id: %d, ",
				// ev.getReferenceId());
				RequestOpenStatisticEx req = statIDList.remove(ev.getReferenceId());
				processed.add(ev.getReferenceId());
				if (req == null) {
					return;
				}

				StatisticObject statObject = req.getStatisticObject();

				logger.printf(Level.TRACE, "Removed from statIDList: req_id=%d ", req.getReferenceId());
				String[] dns = statObject.getObjectId().split("@");
				String status = resolve(ev.getStringValue());

				logger.printf(Level.DEBUG, "Statserver=%s, Switch=%s, DN=%s, type=%s, result=%s\n", this.appname,
						dns[1], dns[0], statObject.getObjectType().toString(), status);
				/* filling in results */

				if (!statsrv.containsKey(this.appname)) {
					statsrv.put(this.appname, new HashMap<String, HashMap>());
				}
				HashMap<String, HashMap> results = statsrv.get(this.appname);

				if (!results.containsKey(dns[1])) //Switch name
					results.put(dns[1], new HashMap<Integer, HashMap>());
				HashMap<String, HashMap> dnlist = results.get(dns[1]);

				if (!dnlist.containsKey(status)) {
					HashMap<String, Object> dnstatuses = new HashMap();
					dnstatuses.put("count", 0);
					// dnstatuses.put("list", new HashMap());
					dnlist.put(status, dnstatuses);
				}
				Integer count = (Integer) dnlist.get(status).get("count");
				count++;
				dnlist.get(status).put("count", count);

				// HashMap statusdnlist=(HashMap)
				// dnlist.get(status).get("list");
				// statusdnlist.put("req_id "+req.getReferenceId(), "DN
				// "+dns[0]);
				// dnlist.get(status).put("list",statusdnlist);

				results.put(dns[1], dnlist);

			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}

	private String resolve(String stringValue) {
		// TODO Auto-generated method stub
		switch (stringValue) {
		case "0":
			return "NotMonitored";
		default:
			return "Monitored";
		}
	}

	public boolean isDone() {
		return statIDList.isEmpty();
	}

	public void printJSON() {
		ObjectMapper mapper = new ObjectMapper();
		String jsonFromMap;
		try {
			jsonFromMap = mapper.writeValueAsString(this.statsrv);
			logger.printf(Level.INFO, "JSON=%s\n", jsonFromMap);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int processed() {
		// TODO Auto-generated method stub
		return processed.size();
	}

	public void empty() {
		processed.clear();
	}

}
