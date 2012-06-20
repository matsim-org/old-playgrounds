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

package playground.wrashid.parkingSearch.withindayFW.parkingTracker;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.api.experimental.events.ActivityEndEvent;
import org.matsim.core.api.experimental.events.ActivityStartEvent;
import org.matsim.core.api.experimental.events.AgentArrivalEvent;
import org.matsim.core.api.experimental.events.AgentDepartureEvent;
import org.matsim.core.api.experimental.events.handler.ActivityEndEventHandler;
import org.matsim.core.api.experimental.events.handler.ActivityStartEventHandler;
import org.matsim.core.api.experimental.events.handler.AgentArrivalEventHandler;
import org.matsim.core.api.experimental.events.handler.AgentDepartureEventHandler;
import org.matsim.core.mobsim.qsim.agents.ExperimentalBasicWithindayAgent;

import playground.wrashid.lib.DebugLib;
import playground.wrashid.lib.GeneralLib;

/**
 * 
 * 
 * @author wrashid
 * 
 */
// Done.
public class CaptureParkingWalkTimesDuringDay implements AgentDepartureEventHandler, AgentArrivalEventHandler {

	private final Map<Id, ExperimentalBasicWithindayAgent> agents;

	private Map<Id, Double> firstParkingWalkTmp = new HashMap<Id, Double>();
	private Map<Id, Double> secondParkingWalkTmp = new HashMap<Id, Double>();

	private Map<Id, Integer> firstParkingActivityPlanElemIndex;
	private Map<Id, Integer> lastParkingActivityPlanElemIndex;

	public CaptureParkingWalkTimesDuringDay(Map<Id, ExperimentalBasicWithindayAgent> agents,
			Map<Id, Integer> firstParkingActivityPlanElemIndex, Map<Id, Integer> lastParkingActivityPlanElemIndex) {
		this.firstParkingActivityPlanElemIndex = firstParkingActivityPlanElemIndex;
		this.lastParkingActivityPlanElemIndex = lastParkingActivityPlanElemIndex;
		this.agents = agents;
		
		for (ExperimentalBasicWithindayAgent agent: agents.values()){
			Id personId=agent.getSelectedPlan().getPerson().getId();
			
			for (PlanElement pe: agent.getSelectedPlan().getPlanElements()){
				if (pe instanceof Leg){
					Leg leg=(Leg) pe;
					
					if (leg.getMode().equals(TransportMode.car)){
						firstParkingWalkTmp.put(personId, 0.0);
						secondParkingWalkTmp.put(personId, 0.0);
						break;
					}
					
				}
			}
		}
	}

	public double getSumBothParkingWalkDurationsInSecond(Id personId) {	
		return firstParkingWalkTmp.get(personId) + secondParkingWalkTmp.get(personId);
	}

	@Override
	public void reset(int iteration) {
		firstParkingWalkTmp.clear();
		secondParkingWalkTmp.clear();
	}

	@Override
	public void handleEvent(AgentArrivalEvent event) {
		Id personId = event.getPersonId();
		ExperimentalBasicWithindayAgent agent = this.agents.get(personId);
		Plan executedPlan = agent.getSelectedPlan();
		int planElementIndex = agent.getCurrentPlanElementIndex();
		
		if (agentHasNoCarLegDuringDay(personId)){
			return;
		}
		
		
		
		double durationFirstWalk = GeneralLib.getIntervalDuration(firstParkingWalkTmp.get(personId), event.getTime());
		double durationSecondWalk = GeneralLib.getIntervalDuration(secondParkingWalkTmp.get(personId), event.getTime());

		if (firstParkingWalkTmp.get(personId)==event.getTime()){
			durationFirstWalk=0.0;
		}
		
		if (secondParkingWalkTmp.get(personId)==event.getTime()){
			durationSecondWalk=0.0;
		}
		
		updateWalkTimeTmpVariables(event.getLegMode(), personId, executedPlan, planElementIndex, durationFirstWalk, durationSecondWalk);
	}

	private boolean agentHasNoCarLegDuringDay(Id personId) {
		
		return firstParkingWalkTmp.get(personId)==null || secondParkingWalkTmp.get(personId)==null;
	}

	private boolean isPlanElementDuringDay(Id personId, int planElementIndex) {
		return planElementIndex > firstParkingActivityPlanElemIndex.get(personId)
				&& planElementIndex < lastParkingActivityPlanElemIndex.get(personId);
	}

	@Override
	public void handleEvent(AgentDepartureEvent event) {
		Id personId = event.getPersonId();
		ExperimentalBasicWithindayAgent agent = this.agents.get(personId);
		Plan executedPlan = agent.getSelectedPlan();
		int planElementIndex = agent.getCurrentPlanElementIndex();
		double startTimeWalkLeg = event.getTime();

		updateWalkTimeTmpVariables(event.getLegMode(), personId, executedPlan, planElementIndex, startTimeWalkLeg, startTimeWalkLeg);
	}

	private void updateWalkTimeTmpVariables(String legMod, Id personId, Plan executedPlan, int planElementIndex,
			double valueA, double valueB) {
		if (isPlanElementDuringDay(personId, planElementIndex)) {
			if (legMod.equals(TransportMode.walk)) {
				Activity previousAct = (Activity) executedPlan.getPlanElements().get(planElementIndex - 1);
				Leg previousLeg = (Leg) executedPlan.getPlanElements().get(planElementIndex - 2);

				if (previousAct.getType().equalsIgnoreCase("parking") && previousLeg.getMode().equals(TransportMode.car)) {
					firstParkingWalkTmp.put(personId, valueA);
				}

				Activity nextAct = (Activity) executedPlan.getPlanElements().get(planElementIndex + 1);
				Leg nextLeg = (Leg) executedPlan.getPlanElements().get(planElementIndex + 2);

				if (nextAct.getType().equalsIgnoreCase("parking") && nextLeg.getMode().equals(TransportMode.car)) {
					secondParkingWalkTmp.put(personId, valueB);
				}
			}
		}
	}

}
