package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.AssociationFactory;
import com.bullhornsdk.data.model.entity.association.standard.CandidateAssociations;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.Category;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.CorporateUser;
import com.bullhornsdk.data.model.entity.core.standard.JobOrder;
import com.bullhornsdk.data.model.entity.core.standard.Lead;
import com.bullhornsdk.data.model.entity.core.standard.Opportunity;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.entity.core.standard.Tearsheet;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

public class AssociationUtilTest {

    @Test
    public void testConstructor() throws IOException {
        AssociationUtil associationUtil = new AssociationUtil();
        Assert.assertNotNull(associationUtil);
    }

    @Test
    public void testGetAssociationFields() throws IOException {
        Assert.assertEquals(AssociationUtil.getAssociationFields(Category.class), AssociationFactory.categoryAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(Category.class), AssociationFactory.categoryAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(ClientContact.class), AssociationFactory.clientContactAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(CorporateUser.class), AssociationFactory.corporateUserAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(JobOrder.class), AssociationFactory.jobOrderAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(Placement.class), AssociationFactory.placementAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(Opportunity.class), AssociationFactory.opportunityAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(Lead.class), AssociationFactory.leadAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(Tearsheet.class), AssociationFactory.tearsheetAssociations().allAssociations());
    }

    @Test
    public void testGetAssociationFieldsBadInput() throws IOException {
        Assert.assertEquals(AssociationUtil.getAssociationFields(AssociationUtilTest.class), new ArrayList<>());
    }

    @Test
    public void testGetCustomObjectAssociationFieldException() throws IOException {
        RestApiException expectedException = new RestApiException("Cannot find association field for association customObjectrs");
        RestApiException actualException = null;

        try {
            AssociationUtil.getCustomObjectAssociationField(EntityInfo.BUSINESS_SECTOR, Candidate.class);
        } catch (RestApiException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void testGetAssociationGetMethodException() throws IOException {
        RestApiException expectedException = new RestApiException("'businessSectors.bogus': 'bogus' does not exist on "
            + "BusinessSector");
        RestApiException actualException = null;

        try {
            AssociationUtil.getAssociationGetMethod(CandidateAssociations.getInstance().businessSectors(), "bogus");
        } catch (RestApiException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }
}
