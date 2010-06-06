package com.nolanlawson.offlinebrowser;

import java.io.File;

public class DummyFile extends File {

	public DummyFile() {
		super("dummy");
	}
	
	

	@Override
	public String getName() {
		return "..";
	}



	@Override
	public boolean isDirectory() {
		return true;
	}
}
