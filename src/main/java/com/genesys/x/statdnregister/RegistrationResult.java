package com.genesys.x.statdnregister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	private StatServerResult ss_result=null;

	private ArrayList processed = new ArrayList();

	private String appname = "";

	private Pattern findswitchregex = Pattern.compile("(?:.(?!@))+$");

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

				String[] dns = dissectResource(statObject.getObjectId());

				String status = resolve(ev.getStringValue());

				logger.printf(Level.DEBUG, "Statserver=%s, Switch=%s, DN=%s, type=%s, result=%s\n", this.appname,
						dns[1], dns[0], statObject.getObjectType().toString(), status);
				/* filling in results old version 

				if (!statsrv.containsKey(this.appname)) {
					statsrv.put(this.appname, new HashMap<String, HashMap>());
				}
				HashMap<String, HashMap> results = statsrv.get(this.appname);

				if (!results.containsKey(dns[1])) // Switch name
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
				*/
				
				if(ss_result==null)
					ss_result=new StatServerResult(this.appname);
				
				SwitchResult sw=ss_result.getSwitch(dns[1]);
				
				sw.updateStatus(status);
				

			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}

	private String[] dissectResource(String objectId) {
		Matcher m = findswitchregex.matcher(objectId);
		if (m.find()) {
			int start = m.start();
			String dn = objectId.substring(0, start);
			String sw = m.group().substring(1);
			return new String[] { dn, sw };
		}

		logger.printf(Level.WARN, "Can't resolve resource name of %s", objectId);
		return new String[] { objectId, "Unknown (Can't resolve!)" };
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

	public void printJSON(long start) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonFromMap;
		logger.printf(Level.INFO,"Time elapsed: %d sec\n", (System.currentTimeMillis()-start)/1000);
		jsonFromMap = this.ss_result.toString();
		logger.printf(Level.INFO, "JSON=%s\n", jsonFromMap);

	}
	
	public static String getJSONString(Map map) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonFromMap="";
		try {
			
			jsonFromMap = mapper.writeValueAsString(map);
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonFromMap;

	}

	public int processed() {
		// TODO Auto-generated method stub
		return processed.size();
	}

	public void empty() {
		processed.clear();
	}
	
	private class StatServerResult {
		public String getName() {
			return name;
		}

		private String name=null;
		private ArrayList<SwitchResult> switches=new ArrayList<SwitchResult>();
		public StatServerResult(String name){
			this.name=name;
		};
		
		public boolean add(SwitchResult sw){
			return switches.add(sw);
		}
		
		public SwitchResult getSwitch(String sw){
			for(SwitchResult s: switches){
				if(s.getName().equals(sw))
					return s;
			}
			
			SwitchResult s=new SwitchResult(sw);
			switches.add(s);
			
			return s;
		}
		
		public String toString(){
			StringBuilder sb=new StringBuilder();
			sb.append("[");
			for (SwitchResult s: switches){
				sb.append(s.toString()).append(",");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("]");
			return "{ \"statserver\":\""+this.name+"\", \"switches\": "+sb.toString()+"}";
		}
	}
	
	private class SwitchResult{
		public String getName() {
			return name;
		}

		public void updateStatus(String state) {
			// TODO Auto-generated method stub
			if (!status.containsKey(state)) {
				status.put(state, 0);
			}
			Integer count = (Integer) status.get(state);
			count++;
			status.put(state, count);
			
		}


		private String name=null;
		private HashMap<String,Integer> status=new HashMap<String,Integer>();
		
		public SwitchResult(String name){
			this.name=name;
		}
		
		public String toString(){
			
			return "{ \"name\":\""+name+"\", \"status\":"+RegistrationResult.getJSONString(status)+"}";
		}
	}

}
