'use strict';

class Reader {
	constructor(buffer) {
		this.input = buffer;
		this.position = 0;
	}

	uint8() {
		return this.input.readUInt8(this.position++);
	}

	uint16BE() {
		var value = this.input.readUInt16BE(this.position);
		this.position += 2;
		return value;
	}

	uint16LE() {
		var value = this.input.readUInt16LE(this.position);
		this.position += 2;
		return value;
	}

	uint32BE() {
		var value = this.input.readUInt32BE(this.position);
		this.position += 4;
		return value;
	}

	uint32LE() {
		var value = this.input.readUInt32LE(this.position);
		this.position += 4;
		return value;
	}

	int8() {
		return this.input.readInt8(this.position++);
	}

	int16BE() {
		var value = this.input.readInt16BE(this.position);
		this.position += 2;
		return value;
	}

	int16LE() {
		var value = this.input.readInt16LE(this.position);
		this.position += 2;
		return value;
	}

	int32BE() {
		var value = this.input.readInt32BE(this.position);
		this.position += 4;
		return value;
	}

	int32LE() {
		var value = this.input.readInt32LE(this.position);
		this.position += 4;
		return value;
	}

	doubleBE() {
		var value = this.input.readDoubleBE(this.position);
		this.position += 8;
		return value;
	}

	doubleLE() {
		var value = this.input.readDoubleLE(this.position);
		this.position += 8;
		return value;
	}

	floatBE() {
		var value = this.input.readFloatBE(this.position);
		this.position += 4;
		return value;
	}

	floatLE() {
		var value = this.input.readFloatLE(this.position);
		this.position += 4;
		return value;
	}

	buffer(length) {
		if (length === 'eof')
			length = this.input.length - this.position;
		var value = this.input.slice(this.position, this.position + length);
		this.position += length;
		return value;
	}
}

module.exports = Reader;
