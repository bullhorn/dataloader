package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.service.csv.Result;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Responsible for tracking REST call result actions.
 */
public class ActionTotals {

    private ConcurrentHashMap<Result.Action, AtomicInteger> concurrentHashMap = new ConcurrentHashMap();

    public ActionTotals() {
        for (Result.Action action : Result.Action.values()) {
            if (!Result.Action.NOT_SET.equals(action)) {
                concurrentHashMap.put(action, new AtomicInteger(0));
            }
        }
    }

    public int getAllActionsTotal() {
        int allActionTotal = 0;
        for (Result.Action action : concurrentHashMap.keySet()) {
            allActionTotal += concurrentHashMap.get(action).intValue();
        }
        return allActionTotal;
    }

    public void incrementActionTotal(Result.Action action) {
        concurrentHashMap.get(action).incrementAndGet();
    }

    public int getActionTotal(Result.Action action) {
        return concurrentHashMap.get(action).intValue();
    }
}
