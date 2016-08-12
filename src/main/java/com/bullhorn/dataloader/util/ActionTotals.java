package com.bullhorn.dataloader.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Responsible for tracking REST call result actions.
 */
public class ActionTotals {

	private AtomicInteger totalUpdate = new AtomicInteger(0);
	private AtomicInteger totalInsert = new AtomicInteger(0);
	private AtomicInteger totalError = new AtomicInteger(0);
	private AtomicInteger totalConvert = new AtomicInteger(0);
	private AtomicInteger totalDelete = new AtomicInteger(0);

	public void incrementTotalInsert() {
		totalInsert.incrementAndGet();
	}

	public void incrementTotalUpdate() {
		totalUpdate.incrementAndGet();
	}

	public void incrementTotalError() {
		totalError.incrementAndGet();
	}

	public void incrementTotalConvert() {
		totalConvert.incrementAndGet();
	}

	public void incrementTotalDelete() {
		totalDelete.incrementAndGet();
	}

	public int getTotalInsert() {
		return totalInsert.intValue();
	}

	public int getTotalUpdate() {
		return totalUpdate.intValue();
	}

	public int getTotalError() {
		return totalError.intValue();
	}

	public int getTotalConvert() {
		return totalConvert.intValue();
	}

	public int getTotalDelete() {
		return totalDelete.intValue();
	}
}