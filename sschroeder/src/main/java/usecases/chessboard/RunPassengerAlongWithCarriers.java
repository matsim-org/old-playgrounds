package usecases.chessboard;

import java.io.File;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlanStrategyManagerFactory;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlReaderV2;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlWriterV2;
import org.matsim.contrib.freight.carrier.CarrierScoringFunctionFactory;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypeLoader;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypeReader;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypes;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.controler.CarrierController;
import org.matsim.contrib.freight.replanning.CarrierReplanningStrategyManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionAccumulator;

import usecases.analysis.CarrierScoreStats;
import usecases.analysis.LegHistogram;
import usecases.chessboard.CarrierScoringFunctionFactoryImpl.DriversActivityScoring;
import usecases.chessboard.CarrierScoringFunctionFactoryImpl.DriversLegScoring;

public class RunPassengerAlongWithCarriers {
	
	public static void main(String[] args) {
		
		createOutputDir();
		
		String configFile = "input/usecases/chessboard/passenger/config.xml" ;
		Config config = ConfigUtils.loadConfig(configFile);
		config.setQSimConfigGroup(new QSimConfigGroup());
		
		Controler controler = new Controler( config );
		
		final Carriers carriers = new Carriers();
		new CarrierPlanXmlReaderV2(carriers).read("input/usecases/chessboard/freight/carrierPlans.xml");
		
		CarrierVehicleTypes types = new CarrierVehicleTypes();
		new CarrierVehicleTypeReader(types).read("input/usecases/chessboard/freight/vehicleTypes.xml");
		new CarrierVehicleTypeLoader(carriers).loadVehicleTypes(types);
		
		CarrierPlanStrategyManagerFactory strategyManagerFactory = createStrategyManagerFactory();
		CarrierScoringFunctionFactory scoringFunctionFactory = createScoringFunctionFactory(carriers,controler.getNetwork());
		
		CarrierController carrierController = new CarrierController(carriers, strategyManagerFactory, scoringFunctionFactory);
		
		controler.addControlerListener(carrierController);
		prepareFreightOutputDataAndStats(controler, carriers);
		controler.setOverwriteFiles(true) ;
		
		controler.run() ;
		
	}

	private static void createOutputDir(){
		File dir = new File("output");
		// if the directory does not exist, create it
		if (!dir.exists()){
		    System.out.println("creating directory ./output");
		    boolean result = dir.mkdir();  
		    if(result) System.out.println("./output created");  
		}
	}
	
	private static void prepareFreightOutputDataAndStats(Controler controler, final Carriers carriers) {
		final LegHistogram freightOnly = new LegHistogram(900);
		freightOnly.setPopulation(controler.getPopulation());
		freightOnly.setInclPop(false);
		final LegHistogram withoutFreight = new LegHistogram(900);
		withoutFreight.setPopulation(controler.getPopulation());
		
		CarrierScoreStats scores = new CarrierScoreStats(carriers, "output/carrier_scores", true);
		
		controler.getEvents().addHandler(withoutFreight);
		controler.getEvents().addHandler(freightOnly);
		controler.addControlerListener(scores);
		controler.addControlerListener(new IterationEndsListener() {
			
			@Override
			public void notifyIterationEnds(IterationEndsEvent event) {
				//write plans
				String dir = event.getControler().getControlerIO().getIterationPath(event.getIteration());
				new CarrierPlanXmlWriterV2(carriers).write(dir + "/" + event.getIteration() + ".carrierPlans.xml");
				
				//write stats
				freightOnly.writeGraphic(dir + "/" + event.getIteration() + ".legHistogram_freight.png");
				freightOnly.reset(event.getIteration());
				
				withoutFreight.writeGraphic(dir + "/" + event.getIteration() + ".legHistogram_withoutFreight.png");
				withoutFreight.reset(event.getIteration());
			}
		});
	}


	private static CarrierScoringFunctionFactory createScoringFunctionFactory(Carriers carriers, final Network network) {
		return new CarrierScoringFunctionFactory() {

			@Override
			public ScoringFunction createScoringFunction(Carrier carrier) {
				ScoringFunctionAccumulator sf = new ScoringFunctionAccumulator();
				DriversLegScoring driverLegScoring = new DriversLegScoring(carrier, network);
				DriversActivityScoring actScoring = new DriversActivityScoring();
				sf.addScoringFunction(driverLegScoring);
				sf.addScoringFunction(actScoring);
				return sf;
			}
			
		};
	}


	private static CarrierPlanStrategyManagerFactory createStrategyManagerFactory() {
		final CarrierReplanningStrategyManager strategyManager = new CarrierReplanningStrategyManager();
		
		return null;
	}

	}
