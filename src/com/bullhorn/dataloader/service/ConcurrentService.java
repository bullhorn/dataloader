package com.bullhorn.dataloader.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.domain.MasterData;
import com.bullhorn.dataloader.util.BullhornAPI;


public class ConcurrentService {
	
	ExecutorService exec;
	String numThreads;
	String entity;
	List<Object> records;
	MasterData masterData;
	BullhornAPI bhapi;
	
	private static Log log = LogFactory.getLog(ConcurrentService.class);
	
	public void runProcess() {
		try {
			
			// configure executor service
			exec = Executors.newFixedThreadPool(Integer.parseInt(numThreads));
			// loop through records
			for (Object obj : records) {
				// generic concurrent import implementation
				// there is a 1-1 mapping of entity and service
				Class<?> serviceClass = Class.forName(this.getClass().getPackage().getName() + "." + entity + "Service");
				// instantiate it
				ConcurrentServiceInterface service = (ConcurrentServiceInterface) serviceClass.newInstance();
				// pass the domain object returned from CSV
				service.setObj(obj);
				// pass master data cache
				service.setMasterData(masterData);
				// pass token
				service.setBhapi(bhapi);
				// execute
				exec.execute((Runnable) service);
			}
			
			// shut the executor service down
			exec.shutdown();
			// null it out after it is terminated
			while (!exec.isTerminated()) {}
			exec = null;
		}	
		// if something fails, shutdown
		catch (Exception e) {
			if (exec != null) exec.shutdown();
			exec = null;
			log.error(e);
		}
	}

	public String getNumThreads() {
		return numThreads;
	}

	public void setNumThreads(String numThreads) {
		this.numThreads = numThreads;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public List<Object> getRecords() {
		return records;
	}

	public void setRecords(List<Object> records) {
		this.records = records;
	}

	public MasterData getMasterData() {
		return masterData;
	}

	public void setMasterData(MasterData masterData) {
		this.masterData = masterData;
	}

	public BullhornAPI getBhapi() {
		return bhapi;
	}

	public void setBhapi(BullhornAPI bhapi) {
		this.bhapi = bhapi;
	}

}
