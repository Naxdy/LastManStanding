package x.naxdy.lms;

public enum LMSEvent
{
	// TODO: pull times from config?
	
	INVULN_DONE(60),
	FEAST_SPAWN(1200), // default is 1200
	FEAST_PREPARE(300); // IMPORTANT: here, the time refers to the RELATIVE amount of time BEFORE FEAST_SPAWN || THIS SHOULD ALWAYS BE 300
	
	private int timeAt;
	
	private LMSEvent(int timeAt)
	{
		this.timeAt = timeAt;
	}
	
	public int getTimeAt()
	{
		return timeAt;
	}

}
