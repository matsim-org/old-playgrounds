

package playground.balac.allcsmodestest.replanning;


import org.matsim.core.config.Config;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.replanning.modules.AbstractMultithreadedModule;
import org.matsim.core.router.TripRouter;
import org.matsim.population.algorithms.PermissibleModesCalculator;
import org.matsim.population.algorithms.PermissibleModesCalculatorImpl;
import org.matsim.population.algorithms.PlanAlgorithm;

/**
 * Changes the transportation mode of all legs of one randomly chosen subtour in a plan to a randomly chosen
 * different mode given a list of possible modes.
 *
 * A subtour is a consecutive subset of a plan which starts and ends at the same link.
 * 
 * Certain modes are considered only if the choice would not require some resource to appear
 * out of thin air. For example, you can only drive your car back from work if you have previously parked it
 * there. These are called chain-based modes.
 * 
 * The assumption is that each chain-based mode requires one resource (car, bike, ...) and that this
 * resource is initially positioned at home. Home is the location of the first activity in the plan.
 * 
 * If the plan initially violates this constraint, this module may (!) repair it. 
 * 
 * @author michaz
 * 
 */
public class SubTourModeChoiceCS extends AbstractMultithreadedModule {
	private PermissibleModesCalculator permissibleModesCalculator;
	
	private final String[] chainBasedModes;
	private final String[] modes;
	
	public SubTourModeChoiceCS(final Config config) {
		this( config.global().getNumberOfThreads(),
				config.subtourModeChoice().getModes(),
				config.subtourModeChoice().getChainBasedModes(),
				config.subtourModeChoice().considerCarAvailability());
	}

	public SubTourModeChoiceCS(
			final int numberOfThreads,
			final String[] modes,
			final String[] chainBasedModes,
			final boolean considerCarAvailability) {
		super(numberOfThreads);
		this.modes = modes.clone();
		this.chainBasedModes = chainBasedModes.clone();
		this.permissibleModesCalculator =
			new PermissibleModesCalculatorImpl(
					this.modes,
					considerCarAvailability);
	}
	
	protected String[] getModes() {
		return modes.clone();
	}

	@Override
	public PlanAlgorithm getPlanAlgoInstance() {
		final TripRouter tripRouter = getReplanningContext().getTripRouter();
		final ChooseRandomLegModeForSubtourCS chooseRandomLegMode =
				new ChooseRandomLegModeForSubtourCS(
						tripRouter.getStageActivityTypes(),
						tripRouter.getMainModeIdentifier(),
						this.permissibleModesCalculator,
						this.modes,
						this.chainBasedModes,
						MatsimRandom.getLocalInstance());
		chooseRandomLegMode.setAnchorSubtoursAtFacilitiesInsteadOfLinks( false );
		return chooseRandomLegMode;
	}

	/**
	 * Decides if a person may use a certain mode of transport. Can be used for car ownership.
	 * 
	 */
	public void setPermissibleModesCalculator(PermissibleModesCalculator permissibleModesCalculator) {
		this.permissibleModesCalculator = permissibleModesCalculator;
	}

}
