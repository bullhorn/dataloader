package com.bullhorn.dataloader.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Responsible for tracking REST call result actions.
 */
public class ActionTotals {

	private static AtomicInteger totalUpdate = new AtomicInteger(0);
	private static AtomicInteger totalInsert = new AtomicInteger(0);
	private static AtomicInteger totalError = new AtomicInteger(0);
	private static AtomicInteger totalDelete = new AtomicInteger(0);

	public void incrementTotalInsert() {
		totalInsert.incrementAndGet();
	}

	public void incrementTotalUpdate() {
		totalUpdate.incrementAndGet();
	}

	public void incrementTotalError() {
		totalError.incrementAndGet();
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

	public int getTotalDelete() {
		return totalDelete.intValue();
	}
}