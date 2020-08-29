// Modules
const Discord = require('discord.js');
const path = require('path');
const fs = require('fs');
const requireAll = require('require-all');
// Bot token
const { token } = require('./config.json');
// Create bot client
const client = new Discord.Client();

// Used to connect to mongoDB
client.mongoose = require('./utils/mongoose');
client.guildSchema = require('./utils/guildS');


// Get all the command files from commands/ and save them in a Discord Collection
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

// Events
// Get all the events file
const events = requireAll({
	dirname: __dirname + '/events',
	filter: /^(?!-)(.+)\.js$/,
});

// Bind the events to the files
for (const name in events) {
	const event = events[name];
	client.on(name, event.bind(null, client));
}

client.launch = Date.now();
client.once('ready', async () => {
	console.log('Help Desk launched!');
	await client.user.setActivity('the Help Desk | hd?help | hd?invite', { type: 'WATCHING'});
	setInterval(async () => {
		await client.user.setActivity('the Help Desk | hd?help | hd?invite', { type: 'WATCHING'});
	}, 3600000);
});

// Listen to raw events to emit messageReactionAdd event on uncached messages
client.on('raw', async event => {
	// Listen only to reactionAdd events
	if(event.t !== 'MESSAGE_REACTION_ADD') return;

	const { d: data } = event;
	if(typeof client.channels.cache.get(data.channel_id) === 'undefined') return;

	const channel = client.channels.cache.get(data.channel_id);

	// if the message is already in the cache, don't re-emit the event
	if (channel.messages.cache.has(data.message_id)) return;

	const user = client.users.cache.get(data.user_id);

	// if you're not on the master branch, use `channel.fetchMessage()`
	const message = await channel.messages.fetch(data.message_id);

	// custom emoji are keyed by IDs, while unicode emoji are keyed by names
	const reaction = message.reactions.cache.get(data.emoji.id || data.emoji.name);

	client.emit('messageReactionAdd', reaction, user);
});
// Login into Discord
client.login(token);
// Connect to mongoDB
client.mongoose.init();


// Client useful properties

// Set of permissions required for Help Desk in order to work
client.requiredPermissions = ['MANAGE_CHANNELS', 'ADD_REACTIONS', 'SEND_MESSAGES', 'MANAGE_MESSAGES', 'EMBED_LINKS', 'READ_MESSAGE_HISTORY', 'USE_EXTERNAL_EMOJIS', 'MANAGE_ROLES', 'ATTACH_FILES', 'MENTION_EVERYONE'];

// Caches
client.helpDesksCache = new Discord.Collection();
// Servers where Astro is stopped
client.stoppedServers = ['264445053596991498'];
// Commands cooldown
client.cooldowns = new Discord.Collection();

// Default Embeds for messages responses
client.failureEmbed = new Discord.MessageEmbed()
	.setColor('#ed0c0c')
	.setTitle('\\❗  **Help Desk Failure** \\❗')
	.setFooter('For support use hd?help');

client.errorEmbed = new Discord.MessageEmbed()
	.setColor('#ed0c0c')
	.setTitle('\\🛠️  **Help Desk Internal Error**')
	.setFooter('For support use hd?help');