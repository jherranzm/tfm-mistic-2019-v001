package edu.uoc.mistic.tfm.jherranzm.util;

import org.apache.commons.lang.RandomStringUtils;

public class RandomStringGenerator {

	public String getRandomString(int l) {
	    return RandomStringUtils.randomAlphanumeric(l);
	}
	
}
