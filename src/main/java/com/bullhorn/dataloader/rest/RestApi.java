package com.bullhorn.dataloader.rest;

import com.bullhornsdk.data.api.BullhornRestCredentials;
import com.bullhornsdk.data.api.StandardBullhornData;
import com.bullhornsdk.data.model.entity.core.type.DeleteEntity;
import com.bullhornsdk.data.model.response.crud.CrudResponse;

/**
 * Extension of the standard SDK-REST BullhornData class for interacting with Bullhorn's REST API
 * <p>
 * Contains any extra REST behavior needed by DataLoader that is outside of SDK-REST.
 * This class is responsible for overriding behavior where needed and calling the RestApiExtension to handle
 * the implementation details.
 */
public class RestApi extends StandardBullhornData {

    private final RestApiExtension restApiExtension;

    public RestApi(BullhornRestCredentials bullhornRestCredentials, RestApiExtension restApiExtension) {
        super(bullhornRestCredentials);
        this.restApiExtension = restApiExtension;
    }

    @Override
    public <C extends CrudResponse, T extends DeleteEntity> C deleteEntity(Class<T> type, Integer id) {
        C crudResponse = super.deleteEntity(type, id);
        return restApiExtension.postDelete(this, crudResponse);
    }
}
