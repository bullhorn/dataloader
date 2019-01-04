package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;

/**
 * Given a command, this class returns a task that can execute part of that command.
 */
public class TaskFactory {

    private final EntityInfo entityInfo;
    private final CsvFileWriter csvFileWriter;
    private final PropertyFileUtil propertyFileUtil;
    private final RestApi restApi;
    private final PrintUtil printUtil;
    private final ActionTotals actionTotals;
    private final CompleteUtil completeUtil;

    public TaskFactory(EntityInfo entityInfo,
                       CsvFileWriter csvFileWriter,
                       PropertyFileUtil propertyFileUtil,
                       RestApi restApi,
                       PrintUtil printUtil,
                       ActionTotals actionTotals,
                       CompleteUtil completeUtil) {
        this.entityInfo = entityInfo;
        this.csvFileWriter = csvFileWriter;
        this.propertyFileUtil = propertyFileUtil;
        this.restApi = restApi;
        this.printUtil = printUtil;
        this.actionTotals = actionTotals;
        this.completeUtil = completeUtil;
    }

    /**
     * Given a command enum, this returns the task that will accomplish part of that command.
     *
     * @param command The user's command
     * @return The corresponding task
     */
    public AbstractTask getTask(Command command, Row row) {
        AbstractTask task = null;
        if (command.equals(Command.CONVERT_ATTACHMENTS)) {
            task = new ConvertAttachmentTask(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, completeUtil);
        } else if (command.equals(Command.DELETE)) {
            task = new DeleteTask(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, completeUtil);
        } else if (command.equals(Command.DELETE_ATTACHMENTS)) {
            task = new DeleteAttachmentTask(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, completeUtil);
        } else if (command.equals(Command.EXPORT)) {
            task = new ExportTask(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, completeUtil);
        } else if (command.equals(Command.LOAD)) {
            task = new LoadTask(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, completeUtil);
        } else if (command.equals(Command.LOAD_ATTACHMENTS)) {
            task = new LoadAttachmentTask(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, completeUtil);
        }
        return task;
    }
}
