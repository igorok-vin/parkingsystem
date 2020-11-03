package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ParkingService.class})
public class ParkingDataBaseTicketUpdatingIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;

    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static InputReaderUtil inputReaderUtil;

   @BeforeAll
    private static void setUp() throws Exception {
       parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        dataBasePrepareService.clearDataBaseEntries();
        inputReaderUtil = PowerMockito.mock(InputReaderUtil.class);
        PowerMockito.when(inputReaderUtil.readSelection()).thenReturn(1);
        PowerMockito.when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    }

    @AfterAll
    private static void tearDown() {
    }

    /*
    * check that a ticket is actualy saved in DB and Parking table is updated with availability
    */

    @Test
    public void testParkingACar() throws Exception {
        ParkingService parkingServiceMock = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingServiceMock.processIncomingVehicle();

        assertTrue(dataBasePrepareService.testDataBase());
    }

}
