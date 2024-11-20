package dadkvs.server.domain.classes;

public class PhaseOneRequest {

	private int phase1config;
	private int phase1index;
	private int phase1timestamp;

	public PhaseOneRequest(int phase1config, int phase1index, int phase1timestamp) {
		this.phase1config = phase1config;
		this.phase1index = phase1index;
		this.phase1timestamp = phase1timestamp;
	}

	public int getPhase1config() { return phase1config; }
	public int getPhase1index() { return phase1index; }
	public int getPhase1timestamp() { return phase1timestamp; }

}
