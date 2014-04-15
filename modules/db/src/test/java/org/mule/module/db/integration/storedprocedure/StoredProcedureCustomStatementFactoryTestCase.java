/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.db.integration.storedprocedure;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.mule.module.db.integration.DbTestUtil.selectData;
import static org.mule.module.db.integration.TestRecordUtil.assertRecords;
import org.mule.api.MuleMessage;
import org.mule.api.client.LocalMuleClient;
import org.mule.module.db.internal.domain.autogeneratedkey.AutoGeneratedKeyStrategy;
import org.mule.module.db.internal.domain.connection.DbConnection;
import org.mule.module.db.internal.domain.query.QueryTemplate;
import org.mule.module.db.internal.domain.statement.QueryStatementFactory;
import org.mule.module.db.integration.AbstractDbIntegrationTestCase;
import org.mule.module.db.integration.TestDbConfig;
import org.mule.module.db.integration.matcher.SupportsReturningStoredProcedureResultsWithoutParameters;
import org.mule.module.db.integration.model.AbstractTestDatabase;
import org.mule.module.db.integration.model.DerbyTestDatabase;
import org.mule.module.db.integration.model.Field;
import org.mule.module.db.integration.model.Record;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

public class StoredProcedureCustomStatementFactoryTestCase extends AbstractDbIntegrationTestCase
{

    private static boolean factoryInvoked;

    public StoredProcedureCustomStatementFactoryTestCase(String dataSourceConfigResource, AbstractTestDatabase testDatabase)
    {
        super(dataSourceConfigResource, testDatabase);
    }

    @Parameterized.Parameters
    public static List<Object[]> parameters()
    {
        return TestDbConfig.getResources();
    }

    @Override
    protected String[] getFlowConfigurationResources()
    {
        return new String[] {"integration/storedprocedure/stored-procedure-custom-statement-factory-config.xml"};
    }

    @Test
    public void testRequestResponse() throws Exception
    {
        LocalMuleClient client = muleContext.getClient();

        MuleMessage response = client.send("vm://testRequestResponse", TEST_MESSAGE, null);
        assertThat(response.getPayload(), is(Map.class));
        Map mapPayload = (Map) response.getPayload();
        int expectedUpdateCount = testDatabase instanceof DerbyTestDatabase ? 0 : 1;
        assertThat((Integer) mapPayload.get("updateCount1"), equalTo(expectedUpdateCount));

        List<Map<String, String>> result = selectData("select * from PLANET where POSITION=4", getDefaultDataSource());
        assertRecords(result, new Record(new Field("NAME", "Mercury"), new Field("POSITION", 4)));

        assertThat(factoryInvoked, equalTo(true));
    }

    @Before
    public void setupStoredProcedure() throws Exception
    {
        assumeThat(getDefaultDataSource(), new SupportsReturningStoredProcedureResultsWithoutParameters());
        testDatabase.createStoredProcedureUpdateTestType1(getDefaultDataSource());
    }

    public static class TestStatementFactory extends QueryStatementFactory
    {

        @Override
        public Statement create(DbConnection connection, QueryTemplate queryTemplate) throws SQLException
        {
            factoryInvoked = true;
            return super.create(connection, queryTemplate);
        }

        @Override
        public Statement create(DbConnection connection, QueryTemplate queryTemplate, AutoGeneratedKeyStrategy autoGeneratedKeyStrategy) throws SQLException
        {
            factoryInvoked = true;
            return super.create(connection, queryTemplate, autoGeneratedKeyStrategy);
        }
    }
}