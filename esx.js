
var	electribe = require ("electribe");
var	fs = require ("fs");

var	ESX = electribe.esx1;

var	buffer = fs.readFileSync ("user.esx");
ESX.parse (buffer);

