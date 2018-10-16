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
		
		private HashMap<String, HashMap> results=new HashMap<String,HashMap>();
		
		private ArrayList processed=new ArrayList();
		
		public void addRequest(RequestOpenStatisticEx req){
			
			statIDList.put(req.getReferenceId(),req);
			
		}
		
		public void add(EventInfo ev){
			try{
	        //if(status instanceof DnStatus){
        	if(ev!=null){
        	//System.out.printf("Event reference id: %d, ", ev.getReferenceId());
        	RequestOpenStatisticEx req=statIDList.remove(ev.getReferenceId());
        	processed.add(ev.getReferenceId());
        	if (req==null){
        		return;
        	}
        	
        	StatisticObject statObject=req.getStatisticObject();
        	
        	logger.printf(Level.DEBUG,"Removed from statIDList: req_id=%d ", req.getReferenceId());
        	String[] dns=statObject.getObjectId().split("@");
        	String status=ev.getStringValue();
        	
        	logger.printf(Level.TRACE,"DN=%s, type=%s, result=%s\n", statObject.getObjectId(), statObject.getObjectType().toString(), status);	
			/* filling in results */
        	
        	if(!results.containsKey(dns[1])) results.put(dns[1], new HashMap<Integer,HashMap>());
        	HashMap<String,HashMap> dnlist=results.get(dns[1]);
        	
        	if(!dnlist.containsKey(status)){
        		HashMap<String, Object> dnstatuses=new HashMap();
        		dnstatuses.put("count",0);
        		dnstatuses.put("list", new HashMap());
        		dnlist.put(status, dnstatuses);
        	}
        	Integer count=(Integer) dnlist.get(status).get("count");
        	count++;
        	dnlist.get(status).put("count",count);
        	
        	HashMap statusdnlist=(HashMap) dnlist.get(status).get("list");
        	statusdnlist.put("req_id "+req.getReferenceId(), "DN "+dns[0]);
        	dnlist.get(status).put("list",statusdnlist);
        	
        	results.put(dns[1],dnlist);
        	
		}
			}catch(Exception ex){
				logger.error(ex.getMessage());
			}
	}
		
		public boolean isDone(){
			return statIDList.isEmpty();
		}
	    public void printJSON(){
			ObjectMapper mapper =new ObjectMapper();
			String jsonFromMap;
			try {
				jsonFromMap = mapper.writeValueAsString(this.results);
				logger.printf(Level.INFO,"JSON=%s\n", jsonFromMap);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    }

		public int processed() {
			// TODO Auto-generated method stub
			return processed.size();
		}
		
		public void empty(){
			processed.clear();
		}
	
}
