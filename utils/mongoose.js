// Node Module
const mongoose = require('mongoose');

module.exports = {
	init: () => {
		const dbOptions = {
			useNewUrlParser: true,
			useUnifiedTopology: true,
			autoIndex: false,
			poolSize: 5,
			connectTimeoutMS: 10000,
			family: 4,
		};
		// Connect to mongoDB with name HelpDesk and with the selected options
		mongoose.connect('mongodb://localhost:27017/HelpDesk', dbOptions);
		mongoose.set('useFindAndModify', false);
		mongoose.Promise = global.Promise;

		// mongoose events
		mongoose.connection.on('connected', () => console.log('Mongoose connection successfully opened!'));

		mongoose.connection.on('err', err => console.log('Mongoose connection error: \n' + err.stack));

		mongoose.connection.on('disconnected', (err) => console.log('Mongoose connection disconnected: \n' + err.stack));
	},
};