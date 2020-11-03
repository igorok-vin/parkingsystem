package com.parkit.parkingsystem.integration.service;

import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DataBasePrepareService {

    DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    public void clearDataBaseEntries(){
        Connection connection = null;
        try{
            connection = dataBaseTestConfig.getConnection();

            //set parking entries to available
            connection.prepareStatement("update parking set available = true").execute();

            //clear ticket entries;
            connection.prepareStatement("truncate table ticket").execute();

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            dataBaseTestConfig.closeConnection(connection);
        }
    }
    public boolean testDataBase(){
        boolean result = false;
        Connection connection = null;
        try{
            connection = dataBaseTestConfig.getConnection();
            PreparedStatement preparedStatement =
                    connection.prepareStatement
                            ("select count(*) from ticket where VEHICLE_REG_NUMBER ='ABCDEF'");
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                result = resultSet.getInt(1)>0;
            }
            dataBaseTestConfig.closeResultSet(resultSet);
            dataBaseTestConfig.closePreparedStatement(preparedStatement);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            dataBaseTestConfig.closeConnection(connection);
        }
        return result;
    }
}
