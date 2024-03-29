package com.bullhorn.dataloader.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import org.apache.commons.lang.WordUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.enums.ErrorInfo;
import com.bullhorn.dataloader.rest.Cache;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.DataLoaderException;
import com.bullhorn.dataloader.util.FileUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;

/**
 * Responsible for converting a single row from a CSV input file.
 */
public class ConvertAttachmentTask extends AbstractTask {

    ConvertAttachmentTask(EntityInfo entityInfo,
                          Row row,
                          CsvFileWriter csvFileWriter,
                          PropertyFileUtil propertyFileUtil,
                          RestApi restApi,
                          PrintUtil printUtil,
                          ActionTotals actionTotals,
                          Cache cache, CompleteUtil completeUtil) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, cache, completeUtil);
    }

    protected Result handle() throws Exception {
        String isResumeValue = row.getValue(StringConsts.IS_RESUME);
        if (isResumeValue != null && (Boolean.parseBoolean(isResumeValue) || isResumeValue.equals("1") || isResumeValue.equalsIgnoreCase("Yes"))) {
            String html = convertAttachmentToHtml();
            writeHtmlToFile(html);
            return Result.convert();
        } else {
            return Result.skip();
        }
    }

    private String convertAttachmentToHtml() throws IOException, SAXException, TikaException {
        ContentHandler handler = new ToXMLContentHandler();
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        File attachmentFile = FileUtil.getAttachmentFile(row);
        InputStream stream;
        try {
            stream = Files.newInputStream(attachmentFile.toPath());
        } catch (NoSuchFileException e) {
            throw new DataLoaderException(ErrorInfo.MISSING_ATTACHMENT_FILE, "Cannot read file from disk: " + attachmentFile.toPath());
        }
        parser.parse(stream, handler, metadata);
        return handler.toString();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void writeHtmlToFile(String convertedHtml) throws IOException {
        String externalId = row.getValue(WordUtils.uncapitalize(entityInfo.getEntityName()) + "." + StringConsts.EXTERNAL_ID);
        String convertedAttachmentPath = propertyFileUtil.getConvertedAttachmentFilepath(entityInfo, externalId);
        File convertedAttachmentFile = new File(convertedAttachmentPath);
        convertedAttachmentFile.getParentFile().mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(convertedAttachmentFile);
        fileOutputStream.write(convertedHtml.getBytes());
        fileOutputStream.flush();
        fileOutputStream.close();
    }
}
