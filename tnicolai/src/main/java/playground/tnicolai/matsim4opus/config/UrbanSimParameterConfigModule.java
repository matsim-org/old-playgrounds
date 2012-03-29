package playground.tnicolai.matsim4opus.config;

import org.matsim.core.config.Module;

public class UrbanSimParameterConfigModule extends Module{
	
	private static final long serialVersionUID = 2L;
	
	public static final String GROUP_NAME = "urbansimParameter";
	
	public static final String POPULATION_SAMPLING_RATE = "populationSampleRate";
	
	public static final String OPPORTUNITY_SAMPLING_RATE = "opportunitySampleRate";
	
	public static final String YEAR = "year";
	
	public static final String OPUS_HOME = "opusHome";
	
	public static final String OPUS_DATA_PATH = "opusDataPath";
	
	public static final String MATSIM4OPUS = "matsim4Opus";
	
	public static final String MATSIM4OPUS_OUTPUT = "matsim4OpusOutput";
	
	public static final String MATSIM4OPUS_TEMP = "matsim4OpusTemp";
	
	public static final String IS_TEST_RUN = "isTestRun";
	
	public static final String TEST_PARAMETER = "testParameter";
	
	public static final String IS_BACKUP_RUN_DATA = "isBackup";
	
	private double populationSampleRate;
	
	private double opportunitySampleRate;
	
	private int year;
	
	private String opusHome;
	
	private String opusDataPath;
	
	private String matsim4Opus;
	
	private String matsim4OpusConfig;
	
	private String matsim4OpusOutput;
	
	private String matsim4OpusTemp;
	
	private String matsim4OpusBackup;
	
	private boolean isTestRun;
	
	private String testParameter;
	
	private boolean isBackup;
	
	public UrbanSimParameterConfigModule(String name){
		super(name);
	}
	
//	public UrbanSimParameterConfigModule(Module urbansimParameter){
//		super(GROUP_NAME);
//		for (Entry<String, String> e : urbansimParameter.getParams().entrySet()) {
//			addParam(e.getKey(), e.getValue());
//		}
//	}
	
	@Override
	public void addParam(String param_name, String value) {
		// not used
	}

	public void setPopulationSampleRate(double sampleRate){
		this.populationSampleRate = sampleRate;
	}
	
	public double getPopulationSampleRate(){
		return this.populationSampleRate;
	}
	
	public void setOpportunitySampleRate(double sampleRate){
		this.opportunitySampleRate = sampleRate;
	}
	
	public double getOpportunitySampleRate() {
		return this.opportunitySampleRate;
	}
	
	public void setYear(int year){
		this.year = year;
	}
	
	public int getYear(){
		return this.year;
	}

	public void setOpusHome(String opusHome){
		this.opusHome = opusHome;
	}
	
	public String getOpusHome(){
		return this.opusHome;
	}
	
	public void setOpusDataPath(String opusDataPath){
		this.opusDataPath = opusDataPath;
	}
	
	public String getOpusDataPath(){
		return this.opusDataPath;
	}
	
	public void setMATSim4Opus(String matsim4Opus){
		this.matsim4Opus = matsim4Opus;
	}
	
	public String getMATSim4Opus(){
		return this.matsim4Opus;
	}
	
	public void setMATSim4OpusConfig(String matsim4OpusConfig){
		this.matsim4OpusConfig = matsim4OpusConfig;
	}
	
	public String getMATSim4OpusConfig(){
		return this.matsim4OpusConfig;
	}
	
	public void setMATSim4OpusOutput(String matsim4OpusOutput){
		this.matsim4OpusOutput = matsim4OpusOutput;
	}
	
	public String getMATSim4OpusOutput(){
		return this.matsim4OpusOutput;
	}
	
	public void setMATSim4OpusTemp(String matsim4OpusTemp){
		this.matsim4OpusTemp = matsim4OpusTemp;
	}
	
	public String getMATSim4OpusTemp(){
		return this.matsim4OpusTemp;
	}
	
	public void setMATSim4OpusBackup(String matsim4OpusBackup){
		this.matsim4OpusBackup = matsim4OpusBackup;
	}
	
	public String getMATSim4OpusBackup(){
		return this.matsim4OpusBackup;
	}
	
	public void setTestRun(boolean isTestRun){
		this.isTestRun = isTestRun;
	}
	
	public boolean isTestRun(){
		return this.isTestRun;
	}
	
	public void setTestParameter(String testParameter){
		this.testParameter = testParameter;
	}
	
	public String getTestParameter(){
		return this.testParameter;
	}
	
	public void setBackup(boolean isBackup){
		this.isBackup = isBackup;
	}
	
	public boolean isBackup(){
		return this.isBackup;
	}
}
