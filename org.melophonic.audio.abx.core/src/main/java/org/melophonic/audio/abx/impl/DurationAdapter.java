package org.melophonic.audio.abx.impl;

import javafx.util.Duration;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DurationAdapter extends XmlAdapter<Double, Duration> {

	@Override
	public Duration unmarshal(Double v) throws Exception {
		return new Duration(v);
	}

	@Override
	public Double marshal(Duration v) throws Exception {
		return v == null ? null : v.toMillis();
	}

}
