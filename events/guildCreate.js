// Used to create embed messages
const { MessageEmbed } = require('discord.js');

module.exports = async (client, guild) => {
	try {
		let inviter;
		let dm = false;
		// Get the name of the user who invited the bot
		guild.fetchAuditLogs({ type: 'BOT_ADD', limit: 1 })
			.then(log => {
				dm = true;
				inviter = log.entries.first().executor;
				inviter.createDM()
					.then(channel => presentationMessage(channel, guild, dm));
			})
			.catch(()=> {
				// Get a valid guild channel where the bot can send its welcome message
				let guildChannel = guild.systemChannel;
				if (!guildChannel || !guildChannel.permissionsFor(guild.me).has(['SEND_MESSAGES', 'EMBED_LINKS'])) guildChannel = guild.channels.cache.find(channel => channel.type === 'text' && channel.permissionsFor(guild.me).has('SEND_MESSAGES'));
				if (guildChannel) presentationMessage(guildChannel, guild, dm);
			});


		const presentationMessage = (channel) => {
			let sent = false;
			// Create the basic embed
			const presentationEmbed = new MessageEmbed()
				.setColor(client.mainColor)
				.setTitle('**Welcome to Help Desk | Quick Start**')
				.setThumbnail('https://cdn.discordapp.com/avatars/621813520589258752/d28b64ab46e9221feb18d53ac9fa8954.png?size=1024&random=5cMW7skPwc')
				.setDescription('**Thank you for inviting me for your server, here is a quick start guide on how to set me up!**\n\n**>** My prefix is `hd?`.\n\n**>** You can use `hd?tutorial` to learn how to set up an #help-desk.\n\n**>** You can get a full list of commands with `hd?help`.\n\n\n*Here are some other useful links:*\n[Support](https://discord.gg/4BTXnXu) | [Invite](https://discord.com/oauth2/authorize?client_id=739796627681837067&scope=bot&permissions=268954832)')
				.setFooter('Developed by </> Giuliopime#4965');
			channel.send(presentationEmbed)
				.then(() => sent = true)
				.catch(() => {
					if(sent) return;
					dm = false;
					let guildChannel = guild.systemChannel;
					if (!guildChannel || !guildChannel.permissionsFor(guild.me).has(['SEND_MESSAGES', 'EMBED_LINKS'])) guildChannel = guild.channels.cache.find(c => c.type === 'text' && c.permissionsFor(guild.me).has('SEND_MESSAGES'));
					if (guildChannel) presentationMessage(guildChannel, guild, dm);
				});
		};
	}
	catch(err) {
		console.log(err);
	}
};