package com.bullhorn.dataloader.service;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.ValidationUtil;
import com.bullhornsdk.data.model.response.resume.ParsedResumeAsEntity;
import com.google.common.collect.Lists;

/**
 * Potential feature enhancements
 *
 * Good error handling with parse failures
 * Batch size limitations
 * File type limitation
 * Optimization / implement dupe check
 * Preview screen before insert new parsed candidates
 * Test run first 1 or 2 resumes and preview results
 */
public class ParseResumeService implements Action {


    private final RestSession restSession;
    private final PropertyFileUtil propertyFileUtil;
    private final PrintUtil printUtil;
    private final CompleteUtil completeUtil;
    private final Timer timer;

    public ParseResumeService(RestSession restSession,
                              PropertyFileUtil propertyFileUtil,
                              PrintUtil printUtil,
                              CompleteUtil completeUtil,
                              Timer timer) {
        this.restSession = restSession;
        this.propertyFileUtil = propertyFileUtil;
        this.printUtil = printUtil;
        this.completeUtil = completeUtil;
        this.timer = timer;
    }

    @Override
    public void run(String[] args) throws IOException {
        ActionTotals actionTotals = new ActionTotals();

        String fileDirectory = getFileDirectoryFromArgs(args);
        List<File> filteredFiles = filterForValidFileTypes(fileDirectory);

        for (int i = 0; i < filteredFiles.size(); i++) {
            File file = filteredFiles.get(i);

            ParsedResumeAsEntity parsedResumeAsEntity = restSession.getRestApi().parseResumeAsNewCandidateSdk(file);

            Result result;
            if (parsedResumeAsEntity.getIsSuccess()) {
                result = Result.insert(parsedResumeAsEntity.getEntityId());
            } else {
                result = new Result(Result.Status.FAILURE, Result.Action.UPDATE, -1, "");
            }
            actionTotals.incrementActionTotal(result.getAction());

            Row row = new Row(file.getName(), i);
            completeUtil.rowComplete(row, result, actionTotals);
        }
    }

    private String getFileDirectoryFromArgs(String[] args) {
        return args[1];
    }

    private List<File> filterForValidFileTypes(String path) {
        File directory = new File(path);
        File[] fileList = directory.listFiles(new FilterFiles(
            StringConsts.ResumeFileType.PDF,
            StringConsts.ResumeFileType.DOC,
            StringConsts.ResumeFileType.DOCX,
            StringConsts.ResumeFileType.HTML));

        if (fileList != null && fileList.length > 0){
            return Arrays.asList(fileList.clone());
        }
        return Lists.newArrayList();
    }

    @Override
    public boolean isValidArguments(String[] args) {
        if (!ValidationUtil.validateNumArgs(args, 2, printUtil)) {
            return false;
        }

        return ValidationUtil.validateResumeFolder(args[1]);
    }

    static class FilterFiles implements FilenameFilter {

        private final String[] extensions;

        public FilterFiles(String... extensions) {
            this.extensions = extensions;
        }

        @Override
        public boolean accept(File dir, String name) {
            for (String ext: extensions) {
                if (name.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        }
    }
}
