package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private  TicketDAO ticketDAO;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO){
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    public void processIncomingVehicle() {
        try{
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if(parkingSpot !=null && parkingSpot.getId() > 0){
                String vehicleRegNumber = getVehichleRegNumber();
                if (getList_ofVihicleRegNumberFromDataBase().contains(vehicleRegNumber)) {
                    System.out.println("Welcome back! As a recurring user of our parking lot, you'll benefit from a 5% discount.");
                    System.out.println();
                }
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot); //allot this parking space and mark it's availability as false

                Date inTime = new Date();
                Ticket ticket = new Ticket();
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                ticketDAO.saveTicket(ticket);
                System.out.println("Generated Ticket and saved in DB");
                System.out.println("Please park your vehicle in spot number:"+parkingSpot.getId());
                System.out.println("Recorded in-time for vehicle number:"+vehicleRegNumber+" is:"+inTime);
            }
        }catch(Exception e){
            logger.error("Unable to process incoming vehicle", e);
        }
    }

    private String getVehichleRegNumber() throws Exception {
        System.out.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }
    public List<String> getList_ofVihicleRegNumberFromDataBase() {
        List<String> nameList = new ArrayList<>();
        try {
            Connection connection = null;
            DataBaseConfig dataBaseConfig = new DataBaseConfig();
            connection = dataBaseConfig.getConnection();

            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery("SELECT * FROM ticket");

            while (results.next()) {
                String name = results.getString("VEHICLE_REG_NUMBER");
                nameList.add(name);
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("SQL statement not included!");
        }
        return nameList;
    }

    public List<String> checkForTwoOrMoreOccurrencesVehicleRegNumberInTheListFromDatabase() {
        List<String> orignalList = new ArrayList<>(getList_ofVihicleRegNumberFromDataBase());
        List<String> resultList;
        resultList = orignalList.stream()
                .filter(e -> Collections.frequency(orignalList, e) >= 2)
                .distinct().collect(Collectors.toList());

        return resultList;
    }

    public ParkingSpot getNextParkingNumberIfAvailable(){
        int parkingNumber=0;
        ParkingSpot parkingSpot = null;
        try{
            ParkingType parkingType = getVehichleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if(parkingNumber > 0){
                parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
            }else{
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        }catch(IllegalArgumentException ie){
            logger.error("Error parsing user input for type of vehicle", ie);
        }catch(Exception e){
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    public ParkingType getVehichleType(){
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch(input){
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
                System.out.println("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }

    public void processExitingVehicle() {
        try{
            String vehicleRegNumber = getVehichleRegNumber();
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
            Date outTime = new Date();
            ticket.setOutTime(outTime);
            Date inHour = ticket.getInTime();
            Date outHour = ticket.getOutTime();
            double duration = (outHour.getTime() - inHour.getTime());
            if(duration <= 1800000){
                 fareCalculatorService.freeParkingFor30min(ticket);
                System.out.println("The parking for free up to 30 min.");
                System.out.println("Looking forward to seeing you again!!!");
            } else if (checkForTwoOrMoreOccurrencesVehicleRegNumberInTheListFromDatabase().contains(vehicleRegNumber)) {
                fareCalculatorService.calculateFareWith5_pecentDiscount(ticket);
                System.out.println("A discount of 5% has been applied for recurring users.");
                System.out.println("Looking forward to seeing you again!!!");
            }else{
                fareCalculatorService.calculateFare(ticket);
            }
            if(ticketDAO.updateTicket(ticket)) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);
                System.out.println("Please pay the parking fare:" + ticket.getPrice());
                System.out.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
            }else{
                System.out.println("Unable to update ticket information. Error occurred");
            }
        }catch(Exception e){
            logger.error("Unable to process exiting vehicle", e);
        }
    }
}
