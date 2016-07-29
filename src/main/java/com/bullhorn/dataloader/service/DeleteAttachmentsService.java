package com.bullhorn.dataloader.service;

/**
 * Delete attachment implementation
 * 
 * @author jlrutledge
 *
 */
public class DeleteAttachmentsService extends AbstractService implements Action {

	@Override
	public void run(String[] args) {
		throw new UnsupportedOperationException("not yet implemented");

	}

	@Override
	public boolean isValidArguments(String[] args) {
		return false;
	}

	@Override
	public void printUsage() {
		printUtil.printUsage();
	}

}
