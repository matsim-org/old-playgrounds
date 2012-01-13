/* *********************************************************************** *
 * project: org.matsim.*
 * Users.java
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

/**
 * 
 */
package playground.ikaddoura.busCorridor.finalDyn;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.config.ConfigUtils;

/**
 * @author Ihab
 *
 */
public class Users {
	
	private final static Logger log = Logger.getLogger(Users.class);

	private double avgExecScore;
	private double scoreSum;
	private int numberOfPtLegs;
	private int numberOfCarLegs;
	private int numberOfWalkLegs;
	private String directoryExtIt;
	private String networkFile;

	public Users(String directoryExtIt, String networkFile) {
		this.directoryExtIt = directoryExtIt;
		this.networkFile = networkFile;
	}

	public void analyzeScores() {
		
		List<Double> scores = new ArrayList<Double>();
		double scoreSum = 0.0;
		
		String outputPlanFile = this.directoryExtIt+"/internalIterations/output_plans.xml.gz";		
		
		Config config = ConfigUtils.createConfig();
		config.plans().setInputFile(outputPlanFile);
		config.network().setInputFile(this.networkFile);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		Population population = scenario.getPopulation();

		for(Person person : population.getPersons().values()){
			double score = person.getSelectedPlan().getScore();
			scores.add(score);
		}
		
		for (Double score : scores){
			scoreSum = scoreSum+score;
		}
		
		this.setAvgExecScore(scoreSum/scores.size());
		this.setScoreSum(scoreSum); // !!! toDo: LogSum !!!
		
		log.info("Users Scores analyzed.");
	}
	 
	public void setAvgExecScore(double avgExecScore) {
		this.avgExecScore = avgExecScore;
	}

	public double getAvgExecScore() {
		return avgExecScore;
	}

	public int getNumberOfPtLegs() {
		return numberOfPtLegs;
	}

	public int getNumberOfCarLegs() {
		return numberOfCarLegs;
	}
	
	public int getNumberOfWalkLegs() {
		return numberOfWalkLegs;
	}

	public void setScoreSum(double scoreSum) {
		this.scoreSum = scoreSum;
	}

	public double getScoreSum() {
		return scoreSum;
	}
	
	public void setNumberOfPtLegs(int numberOfPtLegs) {
		this.numberOfPtLegs = numberOfPtLegs;
	}

	public void setNumberOfCarLegs(int numberOfCarLegs) {
		this.numberOfCarLegs = numberOfCarLegs;
	}

	public void setNumberOfWalkLegs(int numberOfWalkLegs) {
		this.numberOfWalkLegs = numberOfWalkLegs;
	}
}
