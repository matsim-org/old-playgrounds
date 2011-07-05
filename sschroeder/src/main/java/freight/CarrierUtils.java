package freight;

import org.matsim.api.core.v01.Id;
import org.matsim.core.basic.v01.IdImpl;

import playground.mzilske.freight.CarrierCapabilities;
import playground.mzilske.freight.CarrierImpl;
import playground.mzilske.freight.CarrierKnowledge;
import playground.mzilske.freight.CarrierVehicle;
import playground.mzilske.freight.Contract;
import playground.mzilske.freight.Offer;
import playground.mzilske.freight.Shipment;
import playground.mzilske.freight.Shipment.TimeWindow;

public class CarrierUtils {
	
	public static CarrierImpl createCarrier(String id, String depotLinkId){
		CarrierImpl carrier = new CarrierImpl(makeId(id), makeId(depotLinkId));
		carrier.setCarrierCapabilities(new CarrierCapabilities());
		carrier.setKnowledge(new CarrierKnowledge());
		return carrier;
	}

	public static CarrierVehicle createAndAddVehicle(CarrierImpl carrier, String vehicleId, String vehicleLocationId, int vehicleCapacity){
		CarrierVehicle vehicle = new CarrierVehicle(makeId(vehicleId), makeId(vehicleLocationId));
		vehicle.setCapacity(vehicleCapacity);
		if(carrier.getCarrierCapabilities() != null){
			carrier.getCarrierCapabilities().getCarrierVehicles().add(vehicle);
		}
		else{
			CarrierCapabilities caps = new CarrierCapabilities();
			caps.getCarrierVehicles().add(vehicle);
			carrier.setCarrierCapabilities(caps);
		}
		return vehicle;
	}
	
	public static TimeWindow createTimeWindow(long start, long end){
		return makeTW(start,end);
	}
	
	public static Offer createOffer(String carrierId){
		Offer offer = new Offer();
		offer.setCarrierId(makeId(carrierId));
		return offer;
	}
	
	public static Shipment createShipment(Id from, Id to, int size, double startPickup, double endPickup, double startDelivery, double endDelivery){
		TimeWindow startTW = makeTW(startPickup, endPickup);
		TimeWindow endTW = makeTW(startDelivery, endDelivery);
		return new Shipment(from,to,size,startTW,endTW);
	}
	
	private static TimeWindow makeTW(double start, double end) {
		return new TimeWindow(start, end);
	}

	public static void createAndAddContract(CarrierImpl carrier, Shipment shipment, Offer offer){
		Contract contract = new Contract(shipment, offer);
		carrier.getContracts().add(contract);
	}
	
	private static Id makeId(String depotLinkId) {
		return new IdImpl(depotLinkId);
	}
	
	

}
