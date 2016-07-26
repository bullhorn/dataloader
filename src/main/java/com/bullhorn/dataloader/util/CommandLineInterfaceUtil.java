package com.bullhorn.dataloader.util;

import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.api.BullhornRestCredentials;
import com.bullhornsdk.data.api.StandardBullhornData;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by rmcdole on 7/25/2016.
 */
public class CommandLineInterfaceUtil {

    protected BullhornData getBullhornData(PropertyFileUtil propertyFileUtil) throws Exception {
        BullhornData bullhornData = new StandardBullhornData(getBullhornRestCredentials(propertyFileUtil));
        return bullhornData;
    }

    protected BullhornRestCredentials getBullhornRestCredentials(PropertyFileUtil propertyFileUtil) throws Exception {
        BullhornRestCredentials bullhornRestCredentials = new BullhornRestCredentials();
        bullhornRestCredentials.setPassword(propertyFileUtil.getPassword());
        bullhornRestCredentials.setRestAuthorizeUrl(propertyFileUtil.getAuthorizeUrl());
        bullhornRestCredentials.setRestClientId(propertyFileUtil.getClientId());
        bullhornRestCredentials.setRestClientSecret(propertyFileUtil.getClientSecret());
        bullhornRestCredentials.setRestLoginUrl(propertyFileUtil.getLoginUrl());
        bullhornRestCredentials.setRestTokenUrl(propertyFileUtil.getTokenUrl());
        bullhornRestCredentials.setUsername(propertyFileUtil.getUsername());
        bullhornRestCredentials.setRestSessionMinutesToLive("60");
        return bullhornRestCredentials;
    }

    protected PropertyFileUtil getPropertyFileUtil() throws IOException {
        return new PropertyFileUtil("dataloader.properties");
    }

    protected ExecutorService getExecutorService(PropertyFileUtil propertyFileUtil) throws IOException {
        return Executors.newFixedThreadPool(getPropertyFileUtil().getNumThreads());
    }

}
