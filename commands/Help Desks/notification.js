module.exports = {
	// Info
	name: 'notification',
	description: 'Choose a message that HelpDesk will send in a specific channel when a user uses the Special Question (if you include {memberID} in the notification message the bot will automatically replace that with the ID of the member who used the special question)',
	aliases: ['noti'],
	args: /^(<#\d{18}>|\d{18})( *\|\|\| *)([\s\S]+)$/,
	usage: '<#channel or channelID> ||| <message>',
	cooldown: 5,
	// Basic checks
	guildOnly: true,
	chooseDesk: true,
	// Command Category
	helpdesk: true,
	async execute(data, member, message, args, index) {
		const channelID = args[0].replace('<#', '').replace('>', '');
		const channel = member.guild.channels.cache.find(c => c.type === 'text' && c.id === channelID);
		if(!channel) {
			message.client.errorEmbed.setDescription('You didn\'t specify a correct text channel');
			return message.channel.send(message.client.errorEmbed);
		}

		if(args[2].length > 1500) {
			message.client.errorEmbed.setDescription('The message can\'t be longer than 1500 characters');
			return message.channel.send(message.client.errorEmbed);
		}

		data.helpDesks[index].notification = args[2];
		data.helpDesks[index].notificationChannel = channelID;

		await message.client.guildSchema.updateOne({ guildID: message.guild.id }, { $set: { ['helpDesks.' + index]: data.helpDesks[index] } });
		await message.client.caches.hdel('settings', message.guild.id);

		message.client.replyEmbed.setDescription('Successfully added the notification for the Special Question');
		await message.channel.send(message.client.replyEmbed);
	},
};