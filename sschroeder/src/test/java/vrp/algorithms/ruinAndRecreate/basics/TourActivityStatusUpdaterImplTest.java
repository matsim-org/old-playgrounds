package vrp.algorithms.ruinAndRecreate.basics;

import java.util.ArrayList;
import java.util.Collection;

import vrp.VRPTestCase;
import vrp.api.Customer;
import vrp.basics.Tour;

public class TourActivityStatusUpdaterImplTest extends VRPTestCase{
	
	Tour tour;
	
	Tour anotherTour;
	
	TourActivityStatusUpdaterImpl statusUpdater;
	
	public void setUp(){
		init();
		Customer depot = getDepot();
		Customer cust1 = customerMap.get(makeId(0,10));
		Customer cust2 = customerMap.get(makeId(10,0));
		Collection<Customer> tourSequence = new ArrayList<Customer>();
		tourSequence.add(depot);
		tourSequence.add(cust1);
		tourSequence.add(cust2);
		tourSequence.add(depot);
		tour = makeTour(tourSequence);
		
		Customer cust21 = customerMap.get(makeId(0,9));
		Customer cust22 = customerMap.get(makeId(10,0));
		Collection<Customer> anotherTourSequence = new ArrayList<Customer>();
		anotherTourSequence.add(depot);
		anotherTourSequence.add(cust21);
		anotherTourSequence.add(cust22);
		anotherTourSequence.add(depot);
		anotherTour = makeTour(anotherTourSequence);
		
		statusUpdater = new TourActivityStatusUpdaterImpl(costs);
	}
	
	public void tearDown(){
		
	}
	
	public void testCalculatedDistance(){
		statusUpdater.update(tour);
		assertEquals(40.0, tour.costs.distance);
	}
	
	public void testCalculatedCosts(){
		statusUpdater.update(tour);
		assertEquals(40.0, tour.costs.generalizedCosts);
	}
	
	public void testCalculatedTime(){
		statusUpdater.update(tour);
		assertEquals(40.0, tour.costs.time);
	}
	
	public void testCurrentLoadsForTwoPickups(){
		statusUpdater.update(tour);
		assertEquals(0, tour.getActivities().get(0).getCurrentLoad());
		assertEquals(1, tour.getActivities().get(1).getCurrentLoad());
		assertEquals(2, tour.getActivities().get(2).getCurrentLoad());
		assertEquals(2, tour.getActivities().get(3).getCurrentLoad());
	}
	
	public void testCurrentLoadsForPickupAndDelivery(){
		statusUpdater.update(anotherTour);
		assertEquals(1, anotherTour.getActivities().get(0).getCurrentLoad());
		assertEquals(0, anotherTour.getActivities().get(1).getCurrentLoad());
		assertEquals(1, anotherTour.getActivities().get(2).getCurrentLoad());
		assertEquals(1, anotherTour.getActivities().get(3).getCurrentLoad());
	}
	
	public void testCalculatedDistanceForAnotherTour(){
		statusUpdater.update(anotherTour);
		assertEquals(38.0, anotherTour.costs.distance);
	}
	
	public void testCalculatedCostsForAnotherTour(){
		statusUpdater.update(anotherTour);
		assertEquals(38.0, anotherTour.costs.generalizedCosts);
	}
	
	public void testCalculatedTimeForAnotherTour(){
		statusUpdater.update(anotherTour);
		assertEquals(38.0, anotherTour.costs.time);
	}

}
