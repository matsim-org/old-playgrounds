/* *********************************************************************** *
 * project: org.matsim.*
 * CreatePopulation.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.ikaddoura.busCorridor.prepare;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

public class CreatePopulation implements Runnable {
	private Map<String, Coord> zoneGeometries = new HashMap<String, Coord>();
	private Scenario scenario;
	private Population population;
	private String networkFile = "../../shared-svn/studies/ihab/busCorridor/input_final/network.xml";

		
	public static void main(String[] args) {
		CreatePopulation potsdamPopulation = new CreatePopulation();
		potsdamPopulation.run();
		
	}

	public void run(){
		scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		population = scenario.getPopulation();
		
		fillZoneData();
		
		generatePopulation();
		
		PopulationWriter populationWriter = new PopulationWriter(scenario.getPopulation(), scenario.getNetwork());
		populationWriter.write("../../shared-svn/studies/ihab/busCorridor/input_final/population.xml");
	}

	private void fillZoneData() {
		Config config = scenario.getConfig();
		config.network().setInputFile(this.networkFile);
		ScenarioUtils.loadScenario(scenario);
		for (Node node : scenario.getNetwork().getNodes().values()){
			zoneGeometries.put(node.getId().toString(), node.getCoord());
		}
	}
	
	private double calculateNormallyDistributedTime(int i, int abweichung) {
		Random random = new Random();
		//draw two random numbers [0;1] from uniform distribution
		double r1 = random.nextDouble();
		double r2 = random.nextDouble();
		//Box-Muller-Method in order to get a normally distributed variable
		double normal = Math.cos(2 * Math.PI * r1) * Math.sqrt(-2 * Math.log(r2));
		//linear transformation in order to optain N[i,7200²]
		double endTimeInSec = i + abweichung * normal ;
		return endTimeInSec;
	}
	
	private double calculateRandomlyDistributedTime(int i, int abweichung){
		Random random = new Random();
		double rnd1 = random.nextDouble();
		double rnd2 = random.nextDouble();
		double vorzeichen = 0;
		if (rnd1<=0.5){
			vorzeichen = -1.0;
		}
		else {
			vorzeichen = 1.0;
		}
		double endTimeInSec = (i + (rnd2 * abweichung * vorzeichen));
		return endTimeInSec;
	}
	
	private Coord modify(String zone) {
		double xCoord = zoneGeometries.get(zone).getX(); // do nothing
		double yCoord = zoneGeometries.get(zone).getY(); // do nothing
		Coord zoneCoord = scenario.createCoord(xCoord, yCoord);
		return zoneCoord;
	}
	
	private void generatePopulation() {
		generatePeakHomeWorkHomeTripsPt("2", Integer.toString(zoneGeometries.size()-3), 50); // home, work, anzahl
		generatePeakHomeWorkHomeTripsPt(Integer.toString(zoneGeometries.size()-3), "2", 50); // home, work, anzahl
		generatePeakHomeWorkHomeTripsCar("2", Integer.toString(zoneGeometries.size()-3), 50); // home, work, anzahl
		generatePeakHomeWorkHomeTripsCar(Integer.toString(zoneGeometries.size()-3), "2", 50); // home, work, anzahl
		
		for (String zone1 : zoneGeometries.keySet()){
			for (String zone2 : zoneGeometries.keySet()){
				if (zone1 != zone2){ // no one stays in home-zone
					generateConstantHomeWorkHomeTripsPt(zone1, zone2, 1); // home, work, anzahl
					generateConstantHomeWorkHomeTripsCar(zone1, zone2, 1); // home, work, anzahl
				}
				else {}
			}
		}
	}

	private void generatePeakHomeWorkHomeTripsPt(String zone1, String zone2, int quantity) {
		for (int i=0; i<quantity; i++){
			Coord homeLocation = modify(zone1);
			Coord workLocation = modify(zone2);
			
			Person person = population.getFactory().createPerson(createId(zone1, zone2, i, TransportMode.pt+"_peak"));
			Plan plan = population.getFactory().createPlan();
			
			double homeEndTime = calculateNormallyDistributedTime(8*60*60, 1*60*60);

			plan.addActivity(createHome(homeLocation, homeEndTime));
			plan.addLeg(createDriveLegPt());
			plan.addActivity(createWork(workLocation, homeEndTime + (8*60*60)));
			plan.addLeg(createDriveLegPt());
			plan.addActivity(createHome(homeLocation, homeEndTime));
			person.addPlan(plan);
			population.addPerson(person);
		}	
	}
	
	private void generatePeakHomeWorkHomeTripsCar(String zone1, String zone2, int quantity) {
		for (int i=0; i<quantity; i++){
			Coord homeLocation = modify(zone1);
			Coord workLocation = modify(zone2);
			
			Person person = population.getFactory().createPerson(createId(zone1, zone2, i, TransportMode.car+"_peak"));
			Plan plan = population.getFactory().createPlan();
			
			double homeEndTime = calculateNormallyDistributedTime(8*60*60, 1*60*60);
			plan.addActivity(createHome(homeLocation, homeEndTime));
			plan.addLeg(createDriveLegCar());
			plan.addActivity(createWork(workLocation, homeEndTime + (8*60*60)));
			plan.addLeg(createDriveLegCar());
			plan.addActivity(createHome(homeLocation, homeEndTime));
			person.addPlan(plan);
			population.addPerson(person);
		}	
	}
	
	private void generateConstantHomeWorkHomeTripsPt(String zone1, String zone2, int quantity) {
		double homeEndTime = 12.5 * 60 * 60;
		for (int i=0; i<quantity; i++){
			Coord homeLocation = modify(zone1);
			Coord workLocation = modify(zone2);
			
			Person person = population.getFactory().createPerson(createId(zone1, zone2, i, TransportMode.pt+"_const"));
			Plan plan = population.getFactory().createPlan();

			double homeEndTimeRnd = calculateRandomlyDistributedTime((int)homeEndTime, (int) (6.5*60*60));
			plan.addActivity(createHome(homeLocation, homeEndTimeRnd));
			plan.addLeg(createDriveLegPt());
			plan.addActivity(createWork(workLocation, homeEndTimeRnd + (2*60*60)));
			plan.addLeg(createDriveLegPt());
			plan.addActivity(createHome(homeLocation, homeEndTimeRnd));
			person.addPlan(plan);
			population.addPerson(person);
//			homeEndTime = homeEndTime + 12.0*60*60/quantity;
		}	
	}
	
	private void generateConstantHomeWorkHomeTripsCar(String zone1, String zone2, int quantity) {
		double homeEndTime = 12.5 * 60 * 60;
		for (int i=0; i<quantity; i++){
			Coord homeLocation = zoneGeometries.get(zone1);
			Coord workLocation = zoneGeometries.get(zone2);
			
			Person person = population.getFactory().createPerson(createId(zone1, zone2, i, TransportMode.car+"_const"));
			Plan plan = population.getFactory().createPlan();

			double homeEndTimeRnd = calculateRandomlyDistributedTime((int)homeEndTime, (int) (6.5*60*60));
			plan.addActivity(createHome(homeLocation, homeEndTimeRnd));
			plan.addLeg(createDriveLegCar());
			plan.addActivity(createWork(workLocation, homeEndTimeRnd + (2*60*60)));
			plan.addLeg(createDriveLegCar());
			plan.addActivity(createHome(homeLocation, homeEndTimeRnd));
			person.addPlan(plan);
			population.addPerson(person);
//			homeEndTime = homeEndTime + 12.0*60*60/quantity;
		}	
	}
	
	private Leg createDriveLegPt() {
		Leg leg = population.getFactory().createLeg(TransportMode.pt);
		return leg;
	}
	
	private Leg createDriveLegCar() {
		Leg leg = population.getFactory().createLeg(TransportMode.car);
		return leg;
	}

	private Activity createWork(Coord workLocation, double endTime) {
		Activity activity = population.getFactory().createActivityFromCoord("work", workLocation);
		activity.setEndTime(endTime);
		return activity;
	}

	private Activity createHome(Coord homeLocation, double endTime) {
		Activity activity = population.getFactory().createActivityFromCoord("home", homeLocation);
		activity.setEndTime(endTime);
		return activity;
	}

	private Id createId(String zone1, String zone2, int i, String transportMode) {
		return new IdImpl(transportMode + "_" + zone1 + "_" + zone2 + "_" + i);
	}
	
}

