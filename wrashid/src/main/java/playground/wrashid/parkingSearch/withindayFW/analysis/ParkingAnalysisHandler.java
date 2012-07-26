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

package playground.wrashid.parkingSearch.withindayFW.analysis;

import org.matsim.api.core.v01.Id;
import org.matsim.core.controler.Controler;

import playground.wrashid.lib.obj.LinkedListValueHashMap;
import playground.wrashid.lib.obj.Pair;
import playground.wrashid.parkingSearch.withindayFW.core.ParkingInfrastructure;
import playground.wrashid.parkingSearch.withindayFW.parkingOccupancy.ParkingOccupancyStats;

public abstract class ParkingAnalysisHandler {

	protected Controler controler;
	
	public void updateParkingOccupancyStatistics(ParkingOccupancyStats parkingOccupancy, ParkingInfrastructure parkingInfrastructure){
		parkingOccupancy.writeOutParkingOccupanciesTxt(controler,parkingInfrastructure);
		parkingOccupancy.writeOutParkingOccupancySumPng(controler);
	}
	
	public abstract void processParkingWalkTimes(LinkedListValueHashMap<Id, Pair<Id, Double>> parkingWalkTimesLog);
	
	public abstract void processParkingSearchTimes(LinkedListValueHashMap<Id, Pair<Id, Double>> parkingSearchTimeLog);
	public abstract void processParkingCost(LinkedListValueHashMap<Id, Pair<Id, Double>> parkingCostLog);
}
