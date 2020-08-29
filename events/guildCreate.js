const Discord = require('discord.js');

module.exports = async (client, guild) => {
	try {
		if (client.stoppedServers.includes(guild.id)) return;
		// Get the name of the user who invited the bot or keep it blank
		let inviterName = 'Hey,';
		let inviter;
		guild.fetchAuditLogs({ type: 'BOT_ADD', limit: 1 })
			.then(log => {
				inviter = log.entries.first().executor;
				inviterName = 'Hey ' + log.entries.first().executor.username + ',';
				inviter.createDM()
					.then(channel => presentationMessage(channel, guild, inviterName))
			})
			.catch(()=> {
				// Get a valid guild channel where Astro can send its message
				let guildChannel = guild.systemChannel;
				if (!guildChannel || !guildChannel.permissionsFor(guild.me).has('SEND_MESSAGES')) guildChannel = guild.channels.cache.find(channel => channel.type === 'text' && channel.permissionsFor(guild.me).has('SEND_MESSAGES'));
				if (guildChannel) presentationMessage(guildChannel, guild, inviterName);
			})
	}
	catch(err) {
		console.log(err);
	}
};

function sleep(ms) {
	return new Promise(resolve => setTimeout(resolve, ms));
}

function presentationMessage(channel, guild, inviterName){
	let sent = false;
	// Create the basic embed
	const presentationEmbed = new Discord.MessageEmbed()
		.setColor('#5856d9')
		.setTitle('\\🖥️  **Astro Engine Console**')
		.setDescription(`\`\`\`Bash\nC:\\Astro\\Guilds\\${guild.name}>Preparing for Landing...\`\`\``)
		.attachFiles(['./Astro.png'])
		.setThumbnail('attachment://Astro.png');
	channel.send(presentationEmbed)
		.then(async msg => {
			sent = true;
			// If we need to send a embed in the guild we will send it in a dynamic mode where it edits itself to give users time to read, if the embed is sent in DM the bot will just send the whole embed istantly
			const cmd = `\`\`\`C:\\Users\\Astro\\${`Guilds\\${guild.name}` || `DMs\\${inviterName || 'new_user'}`}>`;
			presentationEmbed.setDescription(`${cmd}Landing Successful!\`\`\`\n\n**_${inviterName} thanks for adding Astro to your server!_**\n\u200b`);
			presentationEmbed.addField('Prefix', 'My prefix is `<>` , you can change it using `<>prefix new_prefix`');
			presentationEmbed.addField('Start Here', 'Seems like your server needs some improvement, you can start by running the `<>setup` command.\nIt will initialize the Commands-Chat, a VC Generator, the Astro\'s Interface and a link between the created category and an apposite role.');
			presentationEmbed.addField('Explore Astro Features', 'I\'ll let you explore the bot by yourself, you can see a list of commands using `<>help`');
			presentationEmbed.addField('**One last thing...enjoy!**', '\u200b');
			await msg.edit(presentationEmbed);
		})
		.catch(() => {
			if(sent) return;
			inviterName = 'Hey,';
			// Get a valid guild channel where Astro can send its message
			let guildChannel = guild.systemChannel;
			if (!guildChannel || !guildChannel.permissionsFor(guild.me).has('SEND_MESSAGES')) guildChannel = guild.channels.cache.find(channel => channel.type === 'text' && channel.permissionsFor(guild.me).has('SEND_MESSAGES'));
			if (guildChannel) presentationMessage(guildChannel, guild, inviterName);
		});
}