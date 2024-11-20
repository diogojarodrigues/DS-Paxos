package dadkvs.server.domain.classes;

public class PhaseTwoRequest {

	private int phase2config;
	private int phase2index;
	private int phase2value;
	private int phase2timestamp;

	public PhaseTwoRequest(int phase2config, int phase2index, int phase2value, int phase2timestamp) {
		this.phase2config = phase2config;
		this.phase2index = phase2index;
		this.phase2value = phase2value;
		this.phase2timestamp = phase2timestamp;
	}

	public int getPhase2config() { return phase2config; }
	public int getPhase2index() { return phase2index; }
	public int getPhase2value() { return phase2value; }
	public int getPhase2timestamp() { return phase2timestamp; }

}
