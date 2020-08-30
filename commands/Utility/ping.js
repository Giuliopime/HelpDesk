const Discord = require('discord.js');

module.exports = {
	// Info
	name: 'ping',
	description: 'See Help Desk latency',
	args: false,
	cooldown: 10,
	// Basic checks
	guildOnly: false,
	// Command Category
	utility: true,
	async execute(data, member, message) {
		const ping = Date.now() - message.createdTimestamp;
		const pingEmbed = new Discord.MessageEmbed()
			.setColor(message.client.mainColor)
			.setDescription(`Average Help Desk latency: \`${ping} ms\``);
		await message.channel.send(pingEmbed);
	},
};