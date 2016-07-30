package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.util.PrintUtil;

/**
 * Delete attachment implementation
 * 
 * @author jlrutledge
 *
 */
public class DeleteAttachmentsService extends AbstractService implements Action {

	public DeleteAttachmentsService(PrintUtil printUtil) {
		super(printUtil);
	}

	@Override
	public void run(String[] args) {
		throw new UnsupportedOperationException("not yet implemented");

	}

	@Override
	public boolean isValidArguments(String[] args) {
		return false;
	}
}
