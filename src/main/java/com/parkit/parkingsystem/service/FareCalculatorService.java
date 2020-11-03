package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.util.Date;

public class FareCalculatorService {

    /*Ticket ticket = new Ticket();

    Date inHour = ticket.getInTime();
    Date outHour = ticket.getOutTime();

    double duration = (outHour.getTime() - inHour.getTime());// The difference between two dates in milliseconds*/

    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }
        // DONE: TODO: Some tests are failing here. Need to check if this logic is correct
        Date inHour = ticket.getInTime();
        Date outHour = ticket.getOutTime();

        double duration = (outHour.getTime() - inHour.getTime()); // The difference between two dates in milliseconds
        double hours = duration / (1000 * 60 * 60); // The equation to convert milliseconds to hours


        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(hours * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(hours * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }

    }

    public void freeParkingFor30min(Ticket ticket) {
        Date inHour = ticket.getInTime();
        Date outHour = ticket.getOutTime();
        double duration = (outHour.getTime() - inHour.getTime()); // The difference between two dates in milliseconds
        if (duration <= 1800000) {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    ticket.setPrice(duration*(Fare.CAR_RATE_PER_HOUR = 0));
                    break;
                }
                case BIKE: {
                    ticket.setPrice(duration*(Fare.BIKE_RATE_PER_HOUR = 0));
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unkown Parking Type");
            }

        }

    }
    public void calculateFareWith5_pecentDiscount(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        Date inHour = ticket.getInTime();
        Date outHour = ticket.getOutTime();

        double duration = (outHour.getTime() - inHour.getTime()); // The difference between two dates in milliseconds
        double hours = duration / (1000 * 60 * 60); // The equation to convert milliseconds to hours


        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(hours * Fare.CAR_RATE_PER_HOUR * 0.95);
                break;
            }
            case BIKE: {
                ticket.setPrice(hours * Fare.BIKE_RATE_PER_HOUR * 0.95);
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}