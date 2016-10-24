'use strict';

var Reader = require('./src/reader');
var Writer = require('./src/writer');

function extend() {
	for (var i=1; i<arguments.length; i++) {
		for (var key in arguments[i]) {
			if (!arguments[i].hasOwnProperty(key))
				continue;
			arguments[0][key] = arguments[i][key];
		}
	}
	return arguments[0];
}

function isClassType(f) {
	if (typeof(f) != 'function')
		return false;
	return /^class\s/.test(Function.prototype.toString.call(f));
}

function getAtomicSize(typeName) {
	switch (typeName) {
		case 'int8':
		case 'uint8':
			return 1;

		case 'int16BE':
		case 'int16LE':
		case 'uint16BE':
		case 'uint16LE':
			return 2;

		case 'int32BE':
		case 'int32LE':
		case 'uint32BE':
		case 'uint32LE':
		case 'floatBE':
		case 'floatLE':
			return 4;

		case 'doubleBE':
		case 'doubleLE':
			return 8;

		default:
			throw new Error('Unexpected type: ' + typeName);
	}
}

class Format {
	constructor() {
		this.steps = [];

		var scope = this;
		function addStepFunction(fnName) {
			if (fnName in scope)
				return;
			scope[fnName] = function(name, construct) {
				scope.steps.push({ type: 'data', fn: fnName, name: name, construct: construct });
				return scope;
			};
		}

		var names = Object.getOwnPropertyNames(Reader.prototype);
		for (var i = 0; i < names.length; i++) {
			var member = names[i];
			if (member == 'constructor')
				continue;
			addStepFunction(member);
		}
	}

	buffer(name, length, construct) {
		this.steps.push({ type: 'data', fn: 'buffer', name: name, length: length, construct: construct });
		return this;
	}

	nest(name, format, construct) {
		this.steps.push({ type: 'nest', name: name, format: format, construct: construct });
		return this;
	}

	list(name, count, format) {
		this.steps.push({ type: 'list', name: name,  count: count, format: format });
		return this;
	}

	custom(name, callback) {
		this.steps.push({ type: 'custom', name: name, callback: callback });
		return this;
	}

	length() {
		var length = 0;
		for (var i = 0; i < this.steps.length; i++) {
			var step = this.steps[i];
			if (step.type == 'data') {
				if (step.fn == 'buffer') {
					if (step.length === 'eof')
						throw new Error('Cannot use the predictive length() with variable length sections (.buffer() with EOF)');
					length += step.length;
				}
				else {
					length += getAtomicSize(step.fn);
				}
			}

			if (step.type == 'list') {
				length += step.count * step.format.length();
			}

			if (step.type == 'nest') {
				length += step.format.length();
			}

			if (step.type == 'custom') {
				throw new Error('Cannot use the predictive length() with variable length sections (.custom())');
			}
		}
		return length;
	}

	parse(buffer, reader) {
		if (!reader)
			reader = new Reader(buffer);

		var result = {};

		for (var i = 0; i < this.steps.length; i++) {
			var step = this.steps[i];
			if (step.type == 'data') {
				var f = reader[step.fn];

				var value;
				if (step.fn == 'buffer')
					value = f.apply(reader, [step.length]);
				else
					value = f.apply(reader);

				if (step.construct) {
					value = isClassType(step.construct) ? new step.construct(value) : step.construct(value);
				}
				result[step.name] = value;
			}

			if (step.type == 'list') {
				var list = [];
				for (var j = 0; j < step.count; j++) {
					list.push(step.format.parse(buffer, reader));
				}
				result[step.name] = list;
			}

			if (step.type == 'nest') {
				var value = step.format.parse(buffer, reader);
				if (step.construct) {
					value = isClassType(step.construct) ? new step.construct(value) : step.construct(value);
				}
				result[step.name] = value;
			}

			if (step.type == 'custom') {
				var fmt = step.callback(result);
				if (!(fmt instanceof Format))
					throw new Error('Error: .custom() callback must return an instance of Format');
				result[step.name] = fmt.parse(buffer, reader);
			}
		}

		return result;
	}

	write(data, options) {
		var opt = extend({
			writer: null,
			blocksize: 1024
		}, options);

		var end = false;
		if (!opt.writer) {
			end = true;
			opt.writer = new Writer(parseInt(opt.blocksize));
		}
		var writer = opt.writer;

		for (var i = 0; i < this.steps.length; i++) {
			var step = this.steps[i];

			if (step.type == 'data') {
				var f = writer[step.fn];
				var value = data[step.name];
				if (typeof(value) == 'object' && 'serialize' in value) {
					value = value.serialize();
				}
				f.apply(writer, [value]);
			}

			if (step.type == 'list') {
				var list = data[step.name];
				for (var j = 0; j < list.length; j++) {
					var value = list[j];
					step.format.write(value, opt);
				}
			}

			if (step.type == 'nest') {
				var value = data[step.name];
				if (typeof(value) == 'object' && 'serialize' in value) {
					value = value.serialize();
				}
				step.format.write(value, opt);
			}

			if (step.type == 'custom') {
				var fmt = step.callback(data);
				if (!(fmt instanceof Format))
					throw new Error('Error: .custom() callback must return an instance of Format');
				fmt.write(data[step.name], opt);
			}
		}

		if (end)
			writer.end();
		return writer.output;
	}
}

module.exports = Format;
