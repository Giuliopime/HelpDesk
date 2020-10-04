/*
Node Modules
Use 'npm i' to install them
 */
const Discord = require('discord.js');
const path = require('path');
const fs = require('fs');
const requireAll = require('require-all');


/*
Bot Discord Token
You can create your own bot and use its token,
you can do that on https://discord.com/developers/applications
 */
const { token } = require('./config.json');


// Initialize Bot Client
const client = new Discord.Client();

// Load Redis Caches and bind them to the client as a property
const { caches } = require('./utils/caches');
client.caches = caches;

// Utils to connect to mongoDB and interact with the guildSettings schema
client.mongoose = require('./utils/mongoose');
client.guildSchema = require('./utils/guildS');


// Bot Events
// Get all the events file
const events = requireAll({
	dirname: __dirname + '/events',
	filter: /^(?!-)(.+)\.js$/,
});
// Bind the client events to the files
for (const name in events) {
	const event = events[name];
	client.on(name, event.bind(null, client));
}

// Get all the command files from commands/ and save them in a Discord Collection, then bind them to the client
client.commands = new Discord.Collection();
function getCommands(dir, callback) {
	fs.readdir(dir, (err, files) => {
		if (err) throw err;
		files.forEach((file) => {
			const filepath = path.join(dir, file);
			fs.stat(filepath, (err, stats) => {
				if (stats.isDirectory()) {
					getCommands(filepath, callback);
				}
				else if (stats.isFile() && file.endsWith('.js')) {
					const command = require(`./${filepath}`);
					client.commands.set(command.name, command);
				}
			});
		});
	});
}
getCommands('./commands/');


// When the Bot is ready...
client.once('ready', async () => {
	console.log('Help Desk launched!');

	// Update bot status every hour
	await client.user.setActivity('the Help Desk | hd?help | hd?invite', { type: 'WATCHING' });
	setInterval(async () => {
		await client.user.setActivity('the Help Desk | hd?help | hd?invite', { type: 'WATCHING' });
	}, 3600000);

	/*
	Get the Discord Channel where the bot will send the errors it encounters
	Replace 'channelID' with the ID of the Discord Channel where you want the bot to send errors in.
	*/
	client.errorChannel = client.channels.cache.get('channelID');

	// Save the launch time for the info.js command
	client.launch = Date.now();
});

// Listen to raw events to emit messageReactionAdd event on uncached messages
client.on('raw', async event => {
	// Listen only to reactionAdd events
	if(event.t !== 'MESSAGE_REACTION_ADD') return;

	const { d: data } = event;
	if(typeof client.channels.cache.get(data.channel_id) === 'undefined') return;

	const channel = await client.channels.resolve(data.channel_id);

	// if the message is already in the cache, don't re-emit the event
	if (channel.messages.cache.has(data.message_id)) return;

	const user = await client.users.fetch(data.user_id);

	const message = await channel.messages.fetch(data.message_id);

	// Custom emoji are keyed by IDs, while unicode emoji are keyed by names
	const reaction = await message.reactions.resolve(data.emoji.id || data.emoji.name);

	client.emit('messageReactionAdd', reaction, user);
});

// When the bot encounters an error log it and send it to the errorChannel defined in the bot 'ready' event
client.on('error', async err => {
	console.log(err);
	await client.errorReport(err, 'Error from Global error event');
});

// Login into Discord with the bot token
client.login(token)
	.then(() => console.log('Help Desk logged into Discord...'))
	.catch(console.error);

// Connect to mongoDB
client.mongoose.init();


// Client useful properties and methods

/*
 Error handling
 */
client.errorLogEmbed = new Discord.MessageEmbed().setColor('#ed0c0c');

client.errorReport = async function report(err, message, channel) {
	console.log(err);
	// Send the failure message in the channel where the error happened
	if(channel) await channel.send(client.failureEmbed).catch();
	// Report the error in the bot errorChannel, gotten from the 'ready' event
	if(client.errorChannel) {
		client.errorLogEmbed.setDescription('```js\n' + err.stack.split('\n').slice(0, 3).join('\n') + '```').setTitle(err.stack.split('\n').slice(0, 1).join('\n')).addField('Command', message.content || message);
		await client.errorChannel.send(client.errorLogEmbed);
	}
	// Reset the error embed fields
	client.errorLogEmbed.fields = [];
};


/*
Caches
 */

client.helpDesksCache = new Discord.Collection();
client.cooldowns = new Discord.Collection();

// Function to check the global cooldown for a user
// The user can trigger the bot only once every 0.5 seconds, this is to prevent spamming, especially in the #help-desks
client.checkGCD = async function(userID) {
	// return values:
	//	false --> not on cooldown
	//	true --> on cooldown

	// Get the user cooldown from the cache
	const GCD = await client.caches.hget('gCooldowns', userID);

	// If the user isn't in the cache, cache it with the current timestamps
	if(!GCD) {await client.caches.hset('gCooldowns', userID, Date.now());}
	else{
		// If the last interaction with the bot happened within 0.5 seconds return true
		// This is calculated with the timestamp
		if(Date.now() - GCD < 500) return true;

		// Re-cache the user with the current timestamp
		client.caches.hset('gCooldowns', userID, Date.now());
	}
	return false;
};


// Number emojis
client.helpDeskEmojis = {
	0: '0⃣',
	1: '1⃣',
	2: '2⃣',
	3: '3⃣',
	4: '4⃣',
	5: '5⃣',
	6: '6⃣',
	7: '7⃣',
	8: '8⃣',
	9: '9⃣',
	10: '🔟',
	'?': '❓' };

// Colors
client.mainColor = '#4cc714';

// Default Embeds for message responses
client.failureEmbed = new Discord.MessageEmbed()
	.setColor('#ed0c0c')
	.setTitle('\\❗  **Help Desk Failure** \\❗')
	.setDescription('An error occurred, devs are already tracking the issue.')
	.setFooter('For support use hd?help');

client.errorEmbed = new Discord.MessageEmbed()
	.setColor('#ed0c0c')
	.setTitle('\\🛠️  **Help Desk Internal Error**')
	.setFooter('For support use hd?help');

client.replyEmbed = new Discord.MessageEmbed()
	.setColor(client.mainColor)
	.setFooter('type \'hd?help\' for support');