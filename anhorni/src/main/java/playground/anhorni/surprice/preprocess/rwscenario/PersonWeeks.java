/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package playground.anhorni.surprice.preprocess.rwscenario;

import java.util.ArrayList;
import java.util.TreeMap;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.population.ActivityImpl;

public class PersonWeeks {	
	private Person person;
	private ArrayList<TreeMap<Integer, Plan>> days = new ArrayList<TreeMap<Integer, Plan>>();
	private int currentWeek = - 1;
	private boolean isWorker = false;
	private double pweight = 1.0;
		
	
	public void increaseWeek() {
		this.currentWeek++;
	}
	
	public PersonWeeks(Person person) {
		this.person = person;
		
		for (int i = 0; i < 7; i++) {
			this.days.add(i, new TreeMap<Integer, Plan>());
		}
	}
	
	public Plan getDay(int dow, int week) {
		int w = week;
		while (this.days.get(w).size() < 7 && w < 6) { // week is not complete -> take next week
			w++;
		}
		return this.days.get(w).get(dow);
	}
	
	public void addDay(int dow, Plan plan) {
		if (this.currentWeek < 7) {
			this.days.get(this.currentWeek).put(dow, plan);
		}
	}
		
	public void setIsWorker() {
		for (int i = 0; i < days.size(); i++) {
			for (Plan plan : this.days.get(i).values()) {
				for (PlanElement pe : plan.getPlanElements()) {
					if (pe instanceof Activity) {
						ActivityImpl act = (ActivityImpl)pe;				
						if (act.getType().startsWith("w")) {
							this.isWorker = true;
						}
					}
				}
			}
		}
	}
	public Person getPerson() {
		return this.person;
	}
	public boolean isWorker() {
		return isWorker;
	}
	public double getPweight() {
		return pweight;
	}
	public void setPweight(double pweight) {
		this.pweight = pweight;
	}
	public int getCurrentWeek() {
		return currentWeek;
	}
	public void setCurrentWeek(int currentWeek) {
		this.currentWeek = currentWeek;
	}
}
