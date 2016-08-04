package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.FileEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.file.FileWrapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Responsible for attaching a single row from a CSV input file.
 */
public class LoadAttachmentTask <B extends BullhornEntity> extends AbstractTask<B> {

    public static final String ATTACHMENT = "Attachment";

    public LoadAttachmentTask(Command command,
                              Integer rowNumber,
                              Class<B> entity,
                              LinkedHashMap<String, String> dataMap,
                              CsvFileWriter csvWriter,
                              PropertyFileUtil propertyFileUtil,
                              BullhornData bullhornData,
                              PrintUtil printUtil,
                              ActionTotals actionTotals) {
        super(command, rowNumber, entity, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
    }

    @Override
    public void run() {
        Result result;
        try {
            result = handle();
        } catch(Exception e){
            result = handleFailure(e);
        }
        writeToResultCSV(result);
    }

    private Result handle() throws Exception {
        getAndSetBullhornID((propertyFileUtil.getEntityExistFields(entityClass.getSimpleName())).get());
        addParentEntityIDtoDataMap();
        FileWrapper fileWrapper = attachFile();
        return Result.Insert(fileWrapper.getId());
    }

    private <S extends SearchEntity> void getAndSetBullhornID(List<String> properties) throws Exception {
        if (properties.contains("id")){
            bullhornParentId = Integer.parseInt(dataMap.get("id"));
        } else {
            List<String> propertiesWithValues = Lists.newArrayList();
            for (String property : properties) {
                propertiesWithValues.add(getQueryStatement(property, dataMap.get(property), getFieldType(entityClass, property)));
            }
            String query = Joiner.on(" AND ").join(propertiesWithValues);
            List<S> searchList = bullhornData.search((Class<S>) entityClass, query, Sets.newHashSet("id"), ParamFactory.searchParams()).getData();
            if (!searchList.isEmpty()) {
                bullhornParentId = searchList.get(0).getId();
            } else {
                throw new Exception("Parent Entity not found.");
            }
        }
    }

    private <F extends FileEntity> FileWrapper attachFile() {
        File attachmentFile = new File(dataMap.get(relativeFilePath));
        return bullhornData.addFile((Class<F>) entityClass, bullhornParentId, attachmentFile, attachmentFile.getName(), ParamFactory.fileParams(), false);
    }

}
