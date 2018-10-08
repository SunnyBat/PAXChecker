package com.github.sunnybat.paxchecker.status;

/**
 *
 * @author SunnyBat
 */
public class CheckerInfoOutputCLI implements CheckerInfoOutput {

	@Override
	public void update(String output) {
		if (output != null) {
			System.out.println(output);
		}
	}

}
