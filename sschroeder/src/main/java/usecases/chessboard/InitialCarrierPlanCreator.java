package usecases.chessboard;

import java.util.Collection;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlReaderV2;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlWriterV2;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypeLoader;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypeReader;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypes;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.jsprit.MatsimJspritFactory;
import org.matsim.contrib.freight.jsprit.NetworkBasedTransportCosts;
import org.matsim.contrib.freight.jsprit.NetworkRouter;
import org.matsim.core.config.Config;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

import util.Solutions;

import algorithms.VehicleRoutingAlgorithms;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;

public class InitialCarrierPlanCreator {
	
	private Network network;
	
	public InitialCarrierPlanCreator(Network network) {
		this.network = network;
	}

	public CarrierPlan createPlan(Carrier carrier){
		VehicleRoutingProblem.Builder vrpBuilder = MatsimJspritFactory.createRoutingProblemBuilder(carrier, network);
		NetworkBasedTransportCosts.Builder costsBuilder = NetworkBasedTransportCosts.Builder.newInstance(network, carrier.getCarrierCapabilities().getVehicleTypes());
		NetworkBasedTransportCosts costs = costsBuilder.build();
		vrpBuilder.setRoutingCost(costs);
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/usecases/chessboard/vrpalgo/initialPlanAlgorithm.xml");
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		CarrierPlan plan = MatsimJspritFactory.createPlan(carrier, Solutions.getBest(solutions));
		NetworkRouter.routePlan(plan, costs);
		return plan;
	}
	
	public static void main(String[] args) {
		
		Config config = new Config();
		config.addCoreModules();
		Scenario scenario = ScenarioUtils.createScenario(config);
		new MatsimNetworkReader(scenario).readFile("input/diss/network/grid9x9.xml");
		
		Carriers carriers = new Carriers();
		new CarrierPlanXmlReaderV2(carriers).read("input/usecases/chessboard/freight/carrierPlansWithoutRoutes.xml");
		
		CarrierVehicleTypes types = new CarrierVehicleTypes();
		new CarrierVehicleTypeReader(types).read("input/usecases/chessboard/freight/vehicleTypes.xml");
		new CarrierVehicleTypeLoader(carriers).loadVehicleTypes(types);
		
		for(Carrier carrier : carriers.getCarriers().values()){
			CarrierPlan plan = new InitialCarrierPlanCreator(scenario.getNetwork()).createPlan(carrier);
			carrier.setSelectedPlan(plan);
		}
		
		new CarrierPlanXmlWriterV2(carriers).write("input/usecases/chessboard/freight/carrierPlans.xml");
	}

}
