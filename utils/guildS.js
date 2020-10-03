// Node Module
const mongoose = require('mongoose');

// Set the schema for guild settings and export it
const guildSchema = mongoose.Schema({
	// Server ID
	guildID: String,
	// #help-desks
	helpDesks: Array,
});

module.exports = mongoose.model('guildS', guildSchema);