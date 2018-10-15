package com.genesys.x.statdnregister;

public interface ResultHandler {

	void setTreshold(int i);
	int getTreshold();
	void onResult(RegistrationResult res);
}
