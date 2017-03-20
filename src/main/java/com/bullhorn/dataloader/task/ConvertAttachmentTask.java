package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.BullhornRestApi;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Responsible for converting a single row from a CSV input file.
 */
public class ConvertAttachmentTask<A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> extends AbstractTask<A, E, B> {

    public ConvertAttachmentTask(Command command,
                                 Integer rowNumber,
                                 EntityInfo entityInfo,
                                 Map<String, String> dataMap,
                                 CsvFileWriter csvWriter,
                                 PropertyFileUtil propertyFileUtil,
                                 BullhornRestApi bullhornRestApi,
                                 PrintUtil printUtil,
                                 ActionTotals actionTotals) {
        super(command, rowNumber, entityInfo, dataMap, csvWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
    }

    @Override
    public void run() {
        init();
        Result result;
        try {
            result = handle();
        } catch (Exception e) {
            result = handleFailure(e);
        }
        writeToResultCSV(result);
    }

    private boolean isResume() {
        String isResumeValue = dataMap.get("isResume");
        return Boolean.valueOf(isResumeValue) || isResumeValue.equalsIgnoreCase("1") || isResumeValue.equalsIgnoreCase("Yes");
    }

    private Result handle() throws Exception {
        if (isResume()) {
            String convertedHTML = convertAttachmentToHtml();
            writeHtmlToFile(convertedHTML);
            return Result.Convert();
        } else {
            return Result.Skip();
        }
    }

    protected void writeHtmlToFile(String convertedHTML) throws IOException {
        File convertedAttachmentFile = getFile();
        write(convertedHTML, convertedAttachmentFile);
    }

    protected File getFile() {
        String convertedAttachmentPath = getConvertedAttachmentPath();
        File convertedAttachmentFile = new File(convertedAttachmentPath);
        convertedAttachmentFile.getParentFile().mkdirs();
        return convertedAttachmentFile;
    }

    protected void write(String convertedHTML, File convertedAttachmentFile) throws IOException {
        FileOutputStream fop = new FileOutputStream(convertedAttachmentFile.getAbsoluteFile());
        byte[] convertedHTMLInBytes = convertedHTML.getBytes();
        fop.write(convertedHTMLInBytes);
        fop.flush();
        fop.close();
    }

    protected String getConvertedAttachmentPath() {
        return "convertedAttachments/" + entityClass.getSimpleName() + "/" + getExternalId() + ".html";
    }

    private String getExternalId() {
        return dataMap.get(getCamelCasedClassToString() + ".externalID");
    }

    public String convertAttachmentToHtml() throws IOException, SAXException, TikaException {
        ContentHandler handler = new ToXMLContentHandler();

        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        InputStream stream;
        try {
            stream = new FileInputStream(dataMap.get(StringConsts.RELATIVE_FILE_PATH));
        } catch (NullPointerException e) {
            throw new IOException("Row " + rowNumber + ": Missing the '" + StringConsts.RELATIVE_FILE_PATH + "' column required for convertAttachments");
        }
        parser.parse(stream, handler, metadata);
        return handler.toString();
    }

}
