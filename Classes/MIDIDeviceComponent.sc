MIDIDeviceComponent {
	var <value;
	var <responder;
	var <syncFunction;
	var <chan;
	var <number;
	var <spec;
	var <msgType;
	var <midiIn, <midiOut;
	var <>action;
	var traceResponder;
	var <controllerName;
	var <name;

	*create{arg midiIn, midiOut, chan, number, msgType, name, controllerName;
		var newObj;
		if(msgType == \control14, {
			newObj = MIDIDeviceComponent14BitCC.new(
				midiIn, midiOut, chan, number, name, controllerName);
		}, {
			newObj = this.new(midiIn, midiOut, chan, number, msgType, name, controllerName);
		});
		^newObj;
	}

	*new{arg midiIn, midiOut, chan, number, msgType = \control, name, controllerName;
		^super.new.init(midiIn, midiOut, chan, number, msgType, name, controllerName);
	}

	init{arg midiIn_, midiOut_, chan_, number_, msgType_, name_, controllerName_;
		midiIn = midiIn_;
		midiOut = midiOut_;
		chan = chan_;
		number = number_;
		msgType = msgType_;
		name = name_;
		controllerName = controllerName_;
		value = 0;
		this.prSetupSpec;
		this.prSetupResponderAndSyncFunc;
	}

	prSetupSpec{
		spec = \midi.asSpec;
	}

	prSetupResponderAndSyncFunc{
		responder = MIDIFunc({arg val, num, chan, src;
			this.valueAction_(val);
			this.changed(this, \value);
		}, number, chan, msgType, midiIn.uid);
		syncFunction = switch(msgType,
			\control, {
				{arg comp;
					fork {
						midiOut.control(chan, number, value);
					};
				};
			},
			\noteOn, {arg comp;
				fork {
					midiOut.noteOn(chan, number, value);
				};
			},
			\noteOff, {arg comp;
				fork {
					midiOut.noteOff(chan, number, value);
				};
			}
		);
	}

	value_{arg val;
		if(val.isNumber, {
			value = val.asInteger;
		});
	}

	valueAction_{arg val;
		value = val;
		this.doAction;
	}

	doAction{
		this.action.value(this);
	}

	update{arg theChanged, theChanger, what;
		if(theChanger !== this, {
			var newVal;
			if(what == \value, {
				newVal = theChanged.spec.unmap(theChanged.value);
				newVal = spec.map(newVal).asInteger;
				value = newVal;
				this.refresh;
				"%\n".postf(newVal);
			});
		});
	}

	refresh{
		syncFunction.value(this);
	}

	trace{arg bool = true;
		if(traceResponder.notNil, {
			traceResponder.free;
		});
		if(bool, {
			traceResponder =  MIDIFunc({arg val, num, chan, src;
				"%/% - %".format(controllerName, name, val).postln;
			}, number, chan, msgType, midiIn.uid);
		});

	}
}