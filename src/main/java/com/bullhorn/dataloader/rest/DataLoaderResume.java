package com.bullhorn.dataloader.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

public class DataLoaderResume implements MultipartFile {

    private String name;
    private String contentType;
    private byte[] data;

    private DataLoaderResume() { }

    public static DataLoaderResume getInstance(File file,
                                               String contentType) throws IOException {
        return new DataLoaderResume()
            .name(file.getName())
            .contentType(contentType)
            .data(FileUtils.readFileToByteArray(file));
    }

    private DataLoaderResume name(String name) {
        this.name = name;
        return this;
    }

    private DataLoaderResume contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    private DataLoaderResume data(byte[] data) {
        this.data = data;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return data == null || data.length < 1;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public byte[] getBytes() {
        return data;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public void transferTo(File file) throws IllegalStateException {
    }
}
