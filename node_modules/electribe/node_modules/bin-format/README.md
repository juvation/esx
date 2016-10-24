# bin-format

Simple two way binary format serialization

## Install

```
npm install bin-format
```


## Examples

For more usage examples see [tests](../master/test/index.js).

```javascript
var Format = require('bin-format');
var fmt = new Format()
	.uint8('a')
	.uint8('b')
	.uint8('c')
	.uint8('d');

var object = fmt.parse(new Buffer('deadbeef', 'hex'));
console.log(object);
// { a: 222, b: 173, c: 190, d: 239 }

var output = fmt.write(object);
console.log(output);
// <Buffer de ad be ef>
```

```javascript
var Format = require('bin-format');
var fmt = new Format()
	.buffer('header', 4)
	.nest('chunk', new Format()
		.uint32BE('some_header')
		.list('list', 4, new Format()
			.uint8('hdr')
			.uint8('val')
		)
	)
	.uint16BE('afaf');

const buf = new Buffer('baadf00ddeadbeefff01ff02ff03ff04afaf', 'hex');
var object = fmt.parse(buf);
console.log(require('util').inspect(object, { depth: null }));
// { header: <Buffer ba ad f0 0d>,
//   chunk:
//    { some_header: 3735928559,
//      list:
//       [ { hdr: 255, val: 1 },
//         { hdr: 255, val: 2 },
//         { hdr: 255, val: 3 },
//         { hdr: 255, val: 4 } ] },
//   afaf: 44975 }

var output = fmt.write(object);
console.log(output);
// <Buffer ba ad f0 0d de ad be ef ff 01 ff 02 ff 03 ff 04 af af>
```


## API

* `new Format()` - Creates a new format declaration.

* `parse(buffer)` - Parses the buffer and returns an object.

* `write(object)` - Serializes the object and returns a buffer.

* `length()` - Returns the predicted length of the described format in bytes.
This will throw if there are any `custom` sections anywhere in the format declaration.

### Data fields:

Methods:

* `uint8(name, constructor)` - Declares a uint8 field.
* `uint16BE(name, constructor)` - Declares a big endian uint16 field.
* `uint16LE(name, constructor)` - Declares a little endian uint16 field.
* `uint32BE(name, constructor)` - Declares a big endian uint32 field.
* `uint32LE(name, constructor)` - Declares a little endian uint32 field.
* `int8(name, constructor)` - Declares a int8 field.
* `int16BE(name, constructor)` - Declares a big endian int16 field.
* `int16LE(name, constructor)` - Declares a little endian int16 field.
* `int32BE(name, constructor)` - Declares a big endian int32 field.
* `int32LE(name, constructor)` - Declares a little endian int32 field.
* `floatBE(name, constructor)` - Declares a big endian float (4 bytes) field.
* `floatLE(name, constructor)` - Declares a little endian float (4 bytes) field.
* `doubleBE(name, constructor)` - Declares a big endian double (8 bytes) field.
* `doubleLE(name, constructor)` - Declares a little endian double (8 bytes) field.
* `buffer(name, length, constructor)` - Declares a field for an arbitrary length buffer.
	* `length` - Length of the buffer. To read until the end use `'eof'`.

Arguments:

* `name` - This will be the field name in the resulting object.
* `constructor` - (Optional) Function or Class that will be constructed for that field.


### Control structures:

* `list(name, count, format)` - Declares a list field which contains `count` structures defined by `format`.
* `nest(name, format, constructor)` - Creates a nested structure defined by `format`.
* `custom(name, callback)` - Allows dynamic creation of nested sections.
	* `callback` is a `function(state)` where `state` is the parsed object at the time of parsing when .custom() was invoked.
	* The callback must return a `new Format()`.


### The constructor thing

If the constructor is a function, it is called with the parsed data and expected
to return the data that will be stored in the final object.

If the constructor is a class it is expected to provide this interface:
```javascript
class A {
	constructor(data) {
		/* data is the parsed data */
	}

	serialize() {
		/*
		 * some_data is the data to be written and
		 * is expected to be of the same type as the
		 * data passed to the constructor
		 */
		return some_data;
	}
}
```
