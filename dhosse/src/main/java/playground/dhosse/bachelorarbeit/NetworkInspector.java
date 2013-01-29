package playground.dhosse.bachelorarbeit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.matsim4opus.config.AccessibilityParameterConfigModule;
import org.matsim.contrib.matsim4opus.config.MATSim4UrbanSimConfigurationConverterV4;
import org.matsim.contrib.matsim4opus.config.MATSim4UrbanSimControlerConfigModuleV3;
import org.matsim.contrib.matsim4opus.gis.SpatialGrid;
import org.matsim.contrib.matsim4opus.gis.Zone;
import org.matsim.contrib.matsim4opus.gis.ZoneLayer;
import org.matsim.contrib.matsim4opus.interfaces.MATSim4UrbanSimInterface;
import org.matsim.contrib.matsim4opus.matsim4urbansim.AccessibilityControlerListenerImpl;
import org.matsim.contrib.matsim4opus.matsim4urbansim.ParcelBasedAccessibilityControlerListenerV3;
import org.matsim.contrib.matsim4opus.matsim4urbansim.ZoneBasedAccessibilityControlerListenerV3;
import org.matsim.contrib.matsim4opus.matsim4urbansim.router.PtMatrix;
import org.matsim.contrib.matsim4opus.utils.helperObjects.Benchmark;
import org.matsim.contrib.matsim4opus.utils.io.ReadFromUrbanSimModel;
import org.matsim.contrib.matsim4opus.utils.network.NetworkBoundaryBox;
import org.matsim.core.api.experimental.facilities.ActivityFacilities;
import org.matsim.core.facilities.ActivityFacilitiesImpl;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.utils.charts.BarChart;

public class NetworkInspector {
	
	private final Scenario scenario;
	
	private Map<Id,Double> distances = new HashMap<Id,Double>();
	
	private Logger logger = Logger.getLogger(NetworkInspector.class);
	
	private DecimalFormat df = new DecimalFormat(",##0.00");
	
	private double[] linkCapacities = new double[5];
	
	public NetworkInspector(final Scenario sc){
		
		this.scenario = sc;
		
	}
	
	public void isRoutable(){
		
		logger.info("network contains " + this.scenario.getNetwork().getNodes().size() +
				" nodes and " + this.scenario.getNetwork().getLinks().size() + " links");
		
		NetworkCleaner nc = new NetworkCleaner();
		nc.run(this.scenario.getNetwork());
	}
	
	public void checkNetworkAttributes(boolean checkLinks, boolean checkNodes){
		if(checkLinks){
			try {
				checkLinkAttributes();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(checkNodes){
				checkNodeAttributes();
		}
		
	}
	
	public void accessibilityComputation(boolean parcelBased){
		
		AccessibilityControlerListenerImpl listener;
		
		MATSim4UrbanSimInterface main = new MATSim4UrbanSimInterface() {
			
			@Override
			public boolean isParcelMode() {
				return false;
			}
			
			@Override
			public ReadFromUrbanSimModel getReadFromUrbanSimModel() {
				return null;
			}
			
			@Override
			public double getOpportunitySampleRate() {
				return 0;
			}
		};
		
		NetworkBoundaryBox bbox = new NetworkBoundaryBox();
		
		bbox.setDefaultBoundaryBox(this.scenario.getNetwork());
		
		SpatialGrid grid = new SpatialGrid(bbox.getBoundingBox(), 100);
		
		ActivityFacilitiesImpl parcels = new ActivityFacilitiesImpl();//create facility, aus matsim-bev., strukturdaten etc.
		
		//class schreiben: extends accessibilitycontrolerlistener, notifyshutdown overriden, aggregateOpportunities ersetzen, kein file, kein sample, for-schleife: über facilities iterieren
		
//		this.scenario.getConfig().addModule("module", new AccessibilityParameterConfigModule("module"));
//		System.out.println(this.scenario.getConfig().getModule(AccessibilityParameterConfigModule.GROUP_NAME));
//		MATSim4UrbanSimConfigurationConverterV4 connector = new MATSim4UrbanSimConfigurationConverterV4("./input/config.xml");
		//solution: getter-methode rein, config-modul initialisieren, werte für betas reinsetzen (pt auslassen)/beta_x_tt:-12,rest:0

		
		
		ScenarioImpl sc = (ScenarioImpl) this.scenario;
		
//		Benchmark benchmark = new Benchmark();
		
//		ZoneLayer<Id> startZones = new ZoneLayer<Id>(new Set<Zone<Id>>() {
//		});

		if(parcelBased){
			listener = new ParcelBasedAccessibilityControlerListenerV3(main, null,
					parcels, grid, grid, grid, grid, grid, null, null,
					(ScenarioImpl) this.scenario);
		} else{
			ActivityFacilitiesImpl zones = new ActivityFacilitiesImpl();
			
			listener = new ZoneBasedAccessibilityControlerListenerV3(main, null,
					zones, null, null, (ScenarioImpl) this.scenario);
		}
		
	}
	
	public void checkLinkAttributes() throws IOException{
		logger.info("checking link attributes...");
		
		File file = new File("./test/lengthBelowStats.txt");
		FileWriter writer = new FileWriter(file);
		int writerIndex = 0;
		
		double[] nLanes = new double[6];
		
		for(Link link : this.scenario.getNetwork().getLinks().values()){
			double distance = 
					Math.sqrt(Math.pow(link.getToNode().getCoord().getX() -
							link.getFromNode().getCoord().getX(),2) +
							Math.pow(link.getToNode().getCoord().getY() -
							link.getFromNode().getCoord().getY(), 2));
			
			int numberOfLanes = (int) link.getNumberOfLanes();
			nLanes[numberOfLanes-1]++;
			
			if(link.getCapacity()<=500)
				this.linkCapacities[0]++;
			else if(link.getCapacity()<=1000&&link.getCapacity()>500)
				this.linkCapacities[1]++;
			else if(link.getCapacity()<=1500&&link.getCapacity()>1000)
				this.linkCapacities[2]++;
			else if(link.getCapacity()<=2000&&link.getCapacity()>1500)
				this.linkCapacities[3]++;
			else if(link.getCapacity()>2000)
				this.linkCapacities[4]++;
			
			this.distances.put(link.getId(), distance);
			
			if(link.getLength()<7){
				writer.write("length of link " + link.getId() + " below" +
						" min storage capacity for one vehicle (" + df.format(link.getLength()) +
						" m instead of 7 m)\n");
				writerIndex++;
			}
		}
		writer.close();
		
		logger.info("done.");
		
		if(writerIndex>0){
			logger.warn(writerIndex + " warnings about storage capacity. written to file " + file.getName());
//		System.out.println("nlinks: "+net.getLinks().size());
	}
	
		logger.info("writing link statistics files...");
		
		createLaneStatisticsFiles(nLanes);
		
		createLinkLengthComparisonFile();
		
		createLinkCapacityFiles();
		
		logger.info("done.");

//		System.out.println("nnodes: "+net.getNodes().size());
	}
	
	public void checkNodeAttributes() {
		
		logger.info("checking node attributes...");
		
		double[] inDegrees = new double[10];
		double[] outDegrees = new double[10];
		
		for(Node node : this.scenario.getNetwork().getNodes().values()){
			inDegrees[node.getInLinks().size()-1]++;
			outDegrees[node.getOutLinks().size()-1]++;
		}
		
		logger.info("done.");
		
		logger.info("writing node statistics files...");
		
		try {
			createNodeDegreesFiles(inDegrees, outDegrees);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.info("done.");
		
	}
	
	private void createLaneStatisticsFiles(double[] values) throws IOException {
		
		logger.info("writing lane statistics file...");
		
		File file = new File("./test/laneStatistics.txt");
		FileWriter writer = new FileWriter(file);
		writer.write("Degree");
		for(int i=0;i<values.length;i++){
			writer.write("\t"+ (i+1));
		}
		writer.write("\nnObjects");
		for(int j=0;j<values.length;j++){
			writer.write("\t"+values[j]);
		}
		writer.close();
		
		BarChart chart = new BarChart("Number of lanes", "number of lanes", "number of objects");
		chart.addSeries("nlanes", values);
		chart.saveAsPng("./test/laneStatistics.png", 800, 600);
	}

	private void createLinkLengthComparisonFile() throws IOException {
		
		logger.info("writing length statistics file...");
		
		double length = 0;
		double gLength = 0;

		File file = new File("./test/linkLengthComparison.txt");
		FileWriter writer = new FileWriter(file);
		writer.write("Id\tlength\tactualLength");
		
		for(Id id : this.distances.keySet()){
			writer.write("\n"+id.toString()+"\t"+
					this.scenario.getNetwork().getLinks().get(id).getLength()+
					"\t"+this.distances.get(id));
			length += this.scenario.getNetwork().getLinks().get(id).getLength();
			gLength += this.distances.get(id);
		}
		
		writer.close();
		System.out.println("total length of network: " + length + "m\n"
				+ "geom. length of network: " + gLength + "m");
	}
	
private void createLinkCapacityFiles() throws IOException{
		
		logger.info("writing capacities statistics file...");
		
		//kategorien: 500|1000|1500|2000|>...
		String[] categories = new String[5];
		categories[0]="<=500";
		categories[1]="<=1000";
		categories[2]="<=1500";
		categories[3]="<=2000";
		categories[4]=">2000";
		
		BarChart chart = new BarChart("Link capacities","capacity","number of objects",categories);
		chart.addSeries("capacities", this.linkCapacities);
		chart.saveAsPng("./test/linkCapacities.png", 800, 600);
		
	}

	private void createNodeDegreesFiles(double[] inDegrees, double[] outDegrees) throws IOException {
		
		logger.info("writing node degrees files");
		
		File file = new File("./test/nodeDegrees.txt");
		FileWriter writer = new FileWriter(file);
		writer.write("in/out\t1\t2\t3\t4\t5\t6\t7\t8\t9\t10\nnobjects");
		
		for(int i=0;i<inDegrees.length;i++){
			writer.write("\t"+inDegrees[i]+"/"+outDegrees[i]);
		}
		writer.close();

		BarChart chart = new BarChart("Node degrees", "degree", "number of objects");
		chart.addSeries("in-degrees", inDegrees);
		chart.addSeries("out-degrees", outDegrees);
		chart.saveAsPng("./test/nodeDegrees.png", 800, 600);
	}
	
}
