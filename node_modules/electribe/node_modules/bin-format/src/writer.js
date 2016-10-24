'use strict';

var DEFAULT_BLOCKSIZE = 1024;

function append(source, buffer) {
	if (!source)
		return buffer;
	return Buffer.concat([source, buffer]);
}

function grow(buffer, blocksize) {
	return append(buffer, new Buffer(blocksize));
}

class Writer {
	constructor(blocksize) {
		this.blocksize = blocksize || DEFAULT_BLOCKSIZE;
		this.position = 0;
		this.output = grow(null, this.blocksize);
	}

	allocate(n) {
		while (this.output.length - this.position < n) {
			this.output = grow(this.output, this.blocksize);
		}
	}

	uint8(value) {
		this.allocate(1);
		this.output.writeUInt8(value, this.position++);
	}

	uint16BE(value) {
		this.allocate(2);
		this.output.writeUInt16BE(value, this.position);
		this.position += 2;
	}

	uint16LE(value) {
		this.allocate(2);
		this.output.writeUInt16LE(value, this.position);
		this.position += 2;
	}

	uint32BE(value) {
		this.allocate(4);
		this.output.writeUInt32BE(value, this.position);
		this.position += 4;
	}

	uint32LE(value) {
		this.allocate(4);
		this.output.writeUInt32LE(value, this.position);
		this.position += 4;
	}

	int8(value) {
		this.allocate(1);
		this.output.writeInt8(value, this.position++);
	}

	int16BE(value) {
		this.allocate(2);
		this.output.writeInt16BE(value, this.position);
		this.position += 2;
	}

	int16LE(value) {
		this.allocate(2);
		this.output.writeInt16LE(value, this.position);
		this.position += 2;
	}

	int32BE(value) {
		this.allocate(4);
		this.output.writeInt32BE(value, this.position);
		this.position += 4;
	}

	int32LE(value) {
		this.allocate(4);
		this.output.writeInt32LE(value, this.position);
		this.position += 4;
	}

	floatBE(value) {
		this.allocate(4);
		this.output.writeFloatBE(value, this.position);
		this.position += 4;
	}

	floatLE(value) {
		this.allocate(4);
		this.output.writeFloatLE(value, this.position);
		this.position += 4;
	}

	doubleBE(value) {
		this.allocate(8);
		this.output.writeDoubleBE(value, this.position);
		this.position += 8;
	}

	doubleLE(value) {
		this.allocate(8);
		this.output.writeDoubleLE(value, this.position);
		this.position += 8;
	}

	buffer(value) {
		if (value.length == 0)
			return;
		this.allocate(value.length);
		var b = this.output.slice(this.position, this.position + value.length);
		value.copy(b);
		this.position += value.length;
	}

	end() {
		this.output = this.output.slice(0, this.position);
	}
}

module.exports = Writer;
