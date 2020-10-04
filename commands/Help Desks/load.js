const fetch = require('node-fetch');

module.exports = {
	// Info
	name: 'load',
	description: 'Load a json template for your help-desk',
	aliases: ['loadsettings'],
	cooldown: 5,
	// Basic checks
	guildOnly: true,
	chooseDesk: true,
	// Command Category
	helpdesk: true,
	async execute(data, member, message, args, deskIndex) {
		if(!message.attachments.first()) {
			return message.channel.send(message.client.errorEmbed.setDescription('You need  to upload a .json file, like the one the bot sent you with the `hd?save` command.'));
		}
		const file = message.attachments.first();
		if(file.size > 1000000) {
			return message.channel.send(message.client.errorEmbed.setDescription('The file can\'t be larger than 1MB.'));
		}
		if(!file.name.endsWith('.json')) {
			return message.channel.send(message.client.errorEmbed.setDescription('The file needs to be a json file.'));
		}
		await download(file.url);

		async function download(url) {
			// Get the file
			const response = await fetch(url);
			const json = await response.json();
			// Update the database
			data.helpDesks[deskIndex].embedProperties = json.embedProperties;
			data.helpDesks[deskIndex].fieldsReplies = json.fieldsReplies;
			data.helpDesks[deskIndex].specialQuestion = json.specialQuestion;
			data.helpDesks[deskIndex].specialTrigger = json.specialTrigger;
			data.helpDesks[deskIndex].specialRole = json.specialRole;
			await message.client.guildSchema.updateOne({ guildID: message.guild.id }, { $set: { ['helpDesks.' + deskIndex]: data.helpDesks[deskIndex] } });
			await message.client.caches.hdel('settings', message.guild.id);

			await message.channel.send(message.client.replyEmbed.setDescription('Settings loaded successfully. Use `hd?update` to apply the changes.'));
		}
	},
};