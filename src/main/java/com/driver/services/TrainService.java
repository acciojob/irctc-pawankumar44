package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library

        Train train = new Train();

        //logic of route
        String route = "";
        List<Station> stations = trainEntryDto.getStationRoute();
        for(int i = 0; i<stations.size(); ++i){
            if(i == stations.size()-1){
                route = route + stations.get(i).name();
            }
            else{
                route = route + stations.get(i).name()+",";
            }
        }

        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train = trainRepository.save(train);
        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        String fromRoute = seatAvailabilityEntryDto.getFromStation().name();
        String toRoute = seatAvailabilityEntryDto.getToStation().name();
        int totalSeats = train.getNoOfSeats();
        String[] routes = train.getRoute().split(",");
        int fromIdx = 0;
        int toIdx = 0;
        //find indexes of given from and to route
        for(int i = 0; i<routes.length; ++i){
            String curRoute = routes[i];
            if(curRoute.equals(fromRoute)) fromIdx = i;
            else if(curRoute.equals(toRoute)) toIdx = i;
        }

        int bookedSeats = 0;
        List<Ticket> tickets = train.getBookedTickets();
        for(Ticket ticket : tickets){
            String bookedFrom = ticket.getFromStation().name();
            String bookedTo = ticket.getToStation().name();
            for(int i = fromIdx; i<toIdx; ++i){
                String curRoute = routes[i];
                if(bookedFrom.equals(curRoute) || bookedTo.equals(curRoute)) bookedSeats++;
            }
        }
       return totalSeats-bookedSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Train train = trainRepository.findById(trainId).get();
        //find whether the train is by the given destination or not
        String depart = station.name();
////        String [] routes = train.getRoute().split(",");
////        boolean trainsPassing = false;
////        for(int i = 0; i<routes.length-1; ++i){
////            if(depart.equals(routes[i])){
////                trainsPassing = true;
////                break;
////            }
////        }
////        if(!trainsPassing){
////            throw new Exception("Train is not passing from this station");
////        }
////        else{
////            int count = 0;
////            List<Ticket> tickets = train.getBookedTickets();
////            for(Ticket ticket : tickets){
////                String ticketFromStation = ticket.getFromStation().name();
////                if(ticketFromStation.equals())
////            }
////
////            return count;
////        }

        int count = 0;
        List<Ticket> tickets = train.getBookedTickets();
        for(Ticket ticket : tickets){
            String departTicket = ticket.getFromStation().name();
            if(departTicket.equals(depart)){
                count+=ticket.getPassengersList().size();
            }
        }
        if(count>0) return count;
        else throw new Exception("Train is not passing from this station");
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        int maxAge = 0;
        Train train = trainRepository.findById(trainId).get();
        List<Ticket> tickets = train.getBookedTickets();
        for(Ticket ticket : tickets){
            List<Passenger> passengers = ticket.getPassengersList();
            for(Passenger passenger : passengers){
                maxAge = Math.max(maxAge,passenger.getAge());
            }
        }
        return maxAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Integer> ans = new ArrayList<>();
        String curStation = station.name();

        List<Train> trains = trainRepository.findAll();
        for(Train train : trains){
            //get all routes of the train
            String [] routes = train.getRoute().split(",");
            LocalTime departureTime = train.getDepartureTime();
            for(String route : routes){
                if(route.equals(curStation)){
                    //if its between the time
                    if(!departureTime.isBefore(startTime) && !departureTime.isAfter(endTime)){
                        ans.add(train.getTrainId());
                    }
                }
                //increase departureTime by 1 hour
                departureTime = departureTime.plusHours(1);
            }
        }
        return ans;
    }

}
