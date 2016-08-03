package com.bullhorn.dataloader.service.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Joiner;

public class BullhornApiAssociator {
    private static final Logger log = LogManager.getLogger(BullhornApiAssociator.class);

    private final BullhornAPI bhapi;

    public BullhornApiAssociator(BullhornAPI bhapi) {
        this.bhapi = bhapi;
    }

    public void dissociateEverything(EntityInstance parentEntity, EntityInstance childEntity) throws IOException {
        String associationUrl = getQueryAssociationUrl(parentEntity, childEntity);
        String associationIds = Joiner.on(',').join(getIds(associationUrl));
        EntityInstance toManyAssociations = new EntityInstance(associationIds, childEntity.getEntityName());
        String toManyUrl = bhapi.getModificationAssociationUrl(parentEntity, toManyAssociations);
        DeleteMethod deleteMethod = new DeleteMethod(toManyUrl);
        log.debug("Dissociating: " + toManyUrl);
        bhapi.call(deleteMethod);
    }

    public void associate(EntityInstance parentEntity, EntityInstance associationEntity) throws IOException {
        associateEntity(parentEntity, associationEntity);
    }

    public void associateEntity(EntityInstance parentEntity, EntityInstance childEntity) throws IOException {
        String associationUrl = bhapi.getModificationAssociationUrl(parentEntity, childEntity);
        log.debug("Associating " + associationUrl);
        PutMethod putMethod = new PutMethod(associationUrl);
        bhapi.call(putMethod);
    }

    private String getQueryAssociationUrl(EntityInstance parentEntity, EntityInstance childEntity) {
        return bhapi.getRestURL() + "entity/"
                + parentEntity.getEntityName() + "/"
                + parentEntity.getEntityId()
                + "?fields=" + childEntity.getEntityName()
                + "&BhRestToken=" + bhapi.getBhRestToken();
    }

    private List<String> getIds(String url) throws IOException {
        GetMethod getMethod = new GetMethod(url);
        JSONObject jsonResponse = bhapi.call(getMethod);
        JSONObject data = jsonResponse.getJSONObject("data");
        JSONArray elements = data.getJSONObject(data.keys().next()).getJSONArray("data");

        List<String> identifiers = new ArrayList<>(elements.length());
        for (int i = 0; i < elements.length(); i++) {
            identifiers.add(String.valueOf(elements.getJSONObject(i).getInt("id")));
        }
        return identifiers;
    }
}
