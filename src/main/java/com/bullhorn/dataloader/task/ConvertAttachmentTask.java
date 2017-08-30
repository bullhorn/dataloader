package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import org.apache.commons.lang.WordUtils;
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

/**
 * Responsible for converting a single row from a CSV input file.
 */
public class ConvertAttachmentTask<B extends BullhornEntity> extends AbstractTask<B> {

    public ConvertAttachmentTask(EntityInfo entityInfo,
                                 Row row,
                                 CsvFileWriter csvFileWriter,
                                 PropertyFileUtil propertyFileUtil,
                                 RestApi restApi,
                                 PrintUtil printUtil,
                                 ActionTotals actionTotals) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
    }

    protected Result handle() throws Exception {
        if (isResume()) {
            String convertedHtml = convertAttachmentToHtml();
            writeHtmlToFile(convertedHtml);
            return Result.convert();
        } else {
            return Result.skip();
        }
    }

    private boolean isResume() {
        String isResumeValue = row.getValue("isResume");
        return Boolean.valueOf(isResumeValue) || isResumeValue.equalsIgnoreCase("1") || isResumeValue.equalsIgnoreCase("Yes");
    }

    void writeHtmlToFile(String convertedHtml) throws IOException {
        File convertedAttachmentFile = getFile();
        write(convertedHtml, convertedAttachmentFile);
    }

    protected File getFile() {
        String convertedAttachmentPath = getConvertedAttachmentPath();
        File convertedAttachmentFile = new File(convertedAttachmentPath);
        convertedAttachmentFile.getParentFile().mkdirs();
        return convertedAttachmentFile;
    }

    private void write(String convertedHtml, File convertedAttachmentFile) throws IOException {
        FileOutputStream fop = new FileOutputStream(convertedAttachmentFile.getAbsoluteFile());
        byte[] convertedHtmlInBytes = convertedHtml.getBytes();
        fop.write(convertedHtmlInBytes);
        fop.flush();
        fop.close();
    }

    String getConvertedAttachmentPath() {
        return "convertedAttachments/" + entityInfo.getEntityName() + "/" + getExternalId() + ".html";
    }

    private String getExternalId() {
        return row.getValue(WordUtils.uncapitalize(entityInfo.getEntityName()) + ".externalID");
    }

    String convertAttachmentToHtml() throws IOException, SAXException, TikaException {
        ContentHandler handler = new ToXMLContentHandler();

        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        InputStream stream;
        try {
            stream = new FileInputStream(row.getValue(StringConsts.RELATIVE_FILE_PATH));
        } catch (NullPointerException e) {
            throw new IOException("Missing the '" + StringConsts.RELATIVE_FILE_PATH + "' column required for convertAttachments");
        }
        parser.parse(stream, handler, metadata);
        return handler.toString();
    }
}
