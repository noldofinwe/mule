/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jdbc.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

public class JdbcBridgeFunctionalTestCase extends AbstractJdbcFunctionalTestCase
{

    private static final int TEST_ROWS = 10;
    
    public JdbcBridgeFunctionalTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
        setPopulateTestData(false);
    }

    @Parameters
    public static Collection<Object[]> parameters()
    {
        return Arrays.asList(new Object[][]{
            {ConfigVariant.SERVICE, "jdbc-bridge-service.xml"},
            {ConfigVariant.FLOW, "jdbc-bridge-flow.xml"}
        });
    }          
    
    @Override
    protected void createTable() throws Exception
    {
        execSqlUpdate("CREATE TABLE TEST(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0) NOT NULL PRIMARY KEY,TYPE INTEGER,DATA VARCHAR(255))");
        execSqlUpdate("CREATE TABLE TEST_OUT(ID INTEGER NOT NULL PRIMARY KEY,TYPE INTEGER,DATA VARCHAR(255))");
    }

    @Override
    protected void deleteTable() throws Exception
    {
        execSqlUpdate("DELETE FROM TEST");
        execSqlUpdate("DELETE FROM TEST_OUT");
    }

    @Test
    public void testBridgeSuccess() throws Exception
    {
        List<?> results = execSqlQuery("SELECT * FROM TEST");
        assertEquals(0, results.size());
        doTestBridge();
    }
        
    protected void doTestBridge() throws Exception
    {
        QueryRunner queryRunner = jdbcConnector.getQueryRunner();
        Connection connection = jdbcConnector.getConnection();

        for (int i = 0; i < TEST_ROWS; i++)
        {
            queryRunner.update(connection, "INSERT INTO TEST(TYPE, DATA) VALUES (1, 'Test " + i + "')");
        }
        List<?> results = (List<?>) queryRunner.query(connection, "SELECT * FROM TEST WHERE TYPE = 1", new ArrayListHandler());
        assertEquals(TEST_ROWS, results.size());

        long t0 = System.currentTimeMillis();
        while (true)
        {
            results = (List<?>) queryRunner.query(connection, "SELECT * FROM TEST_OUT", new ArrayListHandler());
            logger.info("Results found: " + results.size());
            if (results.size() >= TEST_ROWS)
            {
                break;
            }
            results = (List<?>) queryRunner.query(connection, "SELECT * FROM TEST WHERE TYPE = 2", new ArrayListHandler());
            logger.info("Locked records found: " + results.size());
            assertTrue(TEST_ROWS >= results.size());            
            
            results = (List<?>) queryRunner.query(connection, "SELECT * FROM TEST WHERE TYPE = 1", new ArrayListHandler());
            logger.info("Original records found: " + results.size());
            assertTrue(TEST_ROWS >= results.size());
            
            assertTrue(System.currentTimeMillis() - t0 < 20000);
            Thread.sleep(500);
        }
    }
}
