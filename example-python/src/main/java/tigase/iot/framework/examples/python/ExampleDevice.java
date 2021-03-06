/*
 * ExampleDevice.java
 *
 * Tigase IoT Framework - Examples
 * Copyright (C) 2011-2018 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.iot.framework.examples.python;

import tigase.iot.framework.devices.AbstractSensor;
import tigase.iot.framework.devices.IConfigurationAware;
import tigase.iot.framework.devices.IExecutorDevice;
import tigase.iot.framework.devices.IValue;
import tigase.kernel.beans.config.ConfigField;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExampleDevice<T extends IValue>
		extends AbstractSensor<T>
		implements IConfigurationAware, IExecutorDevice<T> {

	// This is a configuration option supported by the driver. Its value may be changed during runtime by the user
	// using remote client. You may have more than one configuration field. There is a support for fields of type:
	// long/Long, int/Integer and String.
	//
	// If value of the configuration field is changed and there is a setter for this field it will be called but it
	// is then a developers responsibility to update configuration field value.
	//
	// If your listener needs to react on change of this field value, please create a setter for this field.
	@ConfigField(desc = "Some parameter which can be configured and passed to the python script")
	private String parameter = "12";

	// Path to the python script which should be periodically executed.
	private String script = "/home/pi/script.py";

	public ExampleDevice() {
		// Type and name variables need to be filled with device type id and corresponding device type name. Those can
		// be one of the pairs from the following list:
		//
		// type = "humidity-sensor"; name = "Humidity sensor";
		// type = "light-sensor"; name = "Light sensor";
		// type = "movement-sensor"; name = "Motion sensor";
		// type = "pressure-sensor"; name = "Pressure sensor";
		// type = "temperature-sensor"; name = "Temperature sensor";
		//
		// Label should be a name of the actual sensors used to read data, ie. BH1750
		super(null, null, null);
	}

	@Override
	public void initialize() {
		super.initialize();
	}

	@Override
	public void beforeUnregister() {
		super.beforeUnregister();
	}

	@Override
	public void setValue(T value) {
		// here you should react on the value you received (paramter `value`) and using this value modify state of the
		// physical device controlled by this driver
		//

		try {
			// Execute python script with configured parameter and value which should be set
			if (execute(script, parameter, value.getValue().toString())) {
				// If device state is changed properly call:
				updateValue(value);
				// to notify hub that value was changed
			} else {
				// This will be called if python script return exit code different from 0.
			}
		} catch (Exception ex) {
			//
			// If it was not possible to change value of the physical device, then do nothing.
			// If value was changed but adjustment of value was required, then call updateValue() and as a parameter pass
			// actual value representing current device state.
		}
	}

	// Execute python script and wait for the result
	protected boolean execute(String script, String... args) throws InterruptedException, IOException {
		// In case of Python you need to spawn a python process and read its output and convert it to instance of IValue class.
		Stream<String> command = Stream.of("python", script);
		if (args != null) {
			command = Stream.concat(command, Arrays.stream(args));
		}
		Process process = new ProcessBuilder(command.collect(Collectors.toList())).start();
		return process.waitFor() == 0;
	}
}
