const Discord = require('discord.js');

module.exports = {
	// Info
	name: 'info',
	description: 'Gather information about the current status of Help Desk.',
	aliases: ['stats'],
	args: false,
	cooldown: 20,
	// Basic checks
	guildOnly: false,
	// Command Category
	utility: true,
	async execute(data, member, message) {
		// Get info
		const totalShards = message.client.shard.count;
		const uptime = timeConversion(Date.now() - message.client.launch);
		let totalGuilds = message.client.guilds.cache.size;
		let totalUsers = message.client.users.cache.size;

		message.client.shard.fetchClientValues('guilds.cache.size')
			.then(results => {
				totalGuilds = results;
			})
			.catch(()=>totalGuilds = 'Error gathering Server_Count');
		message.client.shard.broadcastEval('this.guilds.cache.reduce((prev, guild) => prev + guild.memberCount, 0)')
			.then(results => {
				totalUsers = results;
			})
			.catch(()=>totalGuilds = 'Error gathering User_Count');


		const statsEmbed = new Discord.MessageEmbed()
			.setColor('#000')
			.setTitle('**Help Desk Statistics**')
			.setDescription(`**>** Server Count: \`${totalGuilds}\`` +
                    `\n**>** User Count: \`${totalUsers}\`` +
                    `\n**>** Shards Count: \`${totalShards}\`` +
					`\n**>** Up Time: \`${uptime}\`` +
                    '\n\n**>** Version: `Alpha`' +
                    '\n**>** Library: `discord.js`' +
                    '\n**>** Developer: `</> Giuliopime#4965`',
				);
		await message.channel.send(statsEmbed);
	},
};

function timeConversion(millisec) {

	let seconds = (millisec / 1000).toFixed(1);

	let minutes = (millisec / (1000 * 60)).toFixed(1);

	let hours = (millisec / (1000 * 60 * 60)).toFixed(1);

	let days = (millisec / (1000 * 60 * 60 * 24)).toFixed(1);

	if (seconds < 60) {
		return seconds + " Sec";
	} else if (minutes < 60) {
		return minutes + " Min";
	} else if (hours < 24) {
		return hours + " Hrs";
	} else {
		return days + " Days"
	}
}