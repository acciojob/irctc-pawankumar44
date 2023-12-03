package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;

    TrainService trainService = new TrainService();


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db


        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
        //check whether the train is passing through route or not
        String bookFromStation = bookTicketEntryDto.getFromStation().name();
        String bookToStation = bookTicketEntryDto.getToStation().name();
        String[] routes = train.getRoute().split(",");
        int fromIdx = 0, toIdx = 0;
        boolean onRouteFrom = false, onRouteTo = false;
        for(int i = 0; i<routes.length;++i){
            String route = routes[i];
            if(route.equals(bookFromStation)){
                onRouteFrom = true;
                fromIdx = i;
            }
            if (route.equals(bookToStation)) {
                onRouteTo = true;
                toIdx = i;
            }
        }
        if(!onRouteFrom || !onRouteTo) {
            throw new Exception("Invalid stations");
        }

        //check whether the tickets available or not
//        int bookedSeats = 0;
//        int totalSeatInTrain = train.getNoOfSeats();
        List<Ticket> tickets = train.getBookedTickets();
//        for(Ticket ticket : tickets){
//            bookedSeats+=ticket.getPassengersList().size();
//        }
//        int availSeats = totalSeatInTrain-bookedSeats;
//        if(availSeats<bookTicketEntryDto.getNoOfSeats()){
//            throw new Exception("Less tickets are available");
//        }
        int availSeats = trainService.calculateAvailableSeats(
                new SeatAvailabilityEntryDto(train.getTrainId(),bookTicketEntryDto.getFromStation(),
                        bookTicketEntryDto.getToStation()));
        if(availSeats<1){
            throw new Exception("Less tickets are available");
        }

        //calculate the fair
        int fair = Math.abs(toIdx-fromIdx) *300;

        Ticket ticket = new Ticket();
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTrain(train);
        ticket.setTotalFare(fair);
        List<Passenger> passengers = new ArrayList<>();
        for(int pasIds : bookTicketEntryDto.getPassengerIds()){
            Passenger passenger = passengerRepository.findById(pasIds).get();
            passengers.add(passenger);
        }
        ticket.setPassengersList(passengers);
        ticket = ticketRepository.save(ticket);

        //update the train
        tickets.add(ticket);
        train.setBookedTickets(tickets);
        trainRepository.save(train);

        //update the passenger
//        Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
//        List<Ticket> passengerBookedTickets = passenger.getBookedTickets();
//        passengerBookedTickets.add(ticket);
//        passenger.setBookedTickets(passengerBookedTickets);
//        passengerRepository.save(passenger);

       return ticket.getTicketId();

    }
}
