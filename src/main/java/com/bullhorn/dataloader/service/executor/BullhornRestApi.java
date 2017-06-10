package com.bullhorn.dataloader.service.executor;

import com.bullhornsdk.data.api.BullhornRestCredentials;
import com.bullhornsdk.data.api.StandardBullhornData;
import com.bullhornsdk.data.model.entity.core.type.DeleteEntity;
import com.bullhornsdk.data.model.response.crud.CrudResponse;

/**
 * Extension of the standard SDK-REST BullhornData class for interacting with Bullhorn's REST API
 * <p>
 * Contains any extra REST behavior needed by DataLoader that is outside of SDK-REST.
 * This class is responsible for overriding behavior where needed and calling the BullhornRestApiExtension to handle
 * the implementation details.
 */
public class BullhornRestApi extends StandardBullhornData {

    public BullhornRestApi(BullhornRestCredentials bullhornRestCredentials) {
        super(bullhornRestCredentials);
    }

    @Override
    public <C extends CrudResponse, T extends DeleteEntity> C deleteEntity(Class<T> type, Integer id) {
        C crudResponse = super.deleteEntity(type, id);
        return BullhornRestApiExtension.postDelete(this, crudResponse);
    }
}
