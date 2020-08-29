const mongoose = require('mongoose');

const guildSchema = mongoose.Schema({
	// Server info
	guildID: String,
	// Help Desks
	helpDesks: Array,
});

module.exports = mongoose.model('guildS', guildSchema);