package com.bullhorn.dataloader.util;

import com.bullhornsdk.data.model.entity.association.AssociationFactory;
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

public class AssociationUtilTest {

    @Test
    public void testConstructor() throws IOException {
        AssociationUtil associationUtil = new AssociationUtil();
        Assert.assertNotNull(associationUtil);
    }

    @Test
    public void testGetAssociationFields() throws IOException {
        Assert.assertEquals(AssociationUtil.getAssociationFields(Category.class), AssociationFactory.categoryAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(ClientContact.class), AssociationFactory.clientContactAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(CorporateUser.class), AssociationFactory.corporateUserAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(JobOrder.class), AssociationFactory.jobOrderAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(Placement.class), AssociationFactory.placementAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(Opportunity.class), AssociationFactory.opportunityAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(Lead.class), AssociationFactory.leadAssociations().allAssociations());
        Assert.assertEquals(AssociationUtil.getAssociationFields(Tearsheet.class), AssociationFactory.tearsheetAssociations().allAssociations());
    }
}
