const Discord = require('discord.js');

module.exports = {
	// Info
	name: 'update',
	description: 'Update the #help-desk embed.',
	aliases: ['hdupdate'],
	cooldown: 5,
	// Basic checks
	guildOnly: true,
	chooseDesk: true,
	// Command Category
	helpdesk: true,
	// Permissions
	globalPerms: ['MANAGE_ROLES'],
	async execute(data, member, message, args, index) {
		const helpDesk = data.helpDesks[index];
		const hdChannel = await message.guild.channels.resolve(helpDesk.channelID);
		if(!hdChannel) {
			return message.channel.send(message.client.errorEmbed.setDescription('I couldn\'t find the #help-desk channel, if it has been deleted use the `hd?fix` command'));
		}
		const requirePerms = ['ADD_REACTIONS', 'SEND_MESSAGES', 'EMBED_LINKS', 'ATTACH_FILES', 'MANAGE_MESSAGES', 'READ_MESSAGE_HISTORY', 'VIEW_CHANNEL', 'MENTION_EVERYONE'];
		if(!hdChannel.permissionsFor(message.guild.me.id).has(requirePerms)) {
			return message.channel.send(message.client.errorEmbed.setDescription(`I'm missing some important permissions in <#${hdChannel.id}>:\n**>** ${requirePerms.filter(permission => !hdChannel.permissionsFor(message.guild.me.id).has(permission)).join('\n**>** ')}`));
		}
		const hdMessage = await hdChannel.messages.fetch(helpDesk.messageID);
		if(!hdMessage) {
			message.client.errorEmbed.setDescription('I couldn\'t find the #help-desk message embed, if it has been deleted use the `hd?fix` command');
			return message.channel.send(message.client.errorEmbed);
		}
		const tempMessage = await message.channel.send('Loading the help desk...');
		await hdMessage.reactions.removeAll();
		const embedProperties = helpDesk.embedProperties;
		const newEmbed = new Discord.MessageEmbed();
		if(embedProperties.title) newEmbed.setTitle(embedProperties.title);
		if(embedProperties.url) newEmbed.setURL(embedProperties.url);
		if(embedProperties.description) newEmbed.setDescription(embedProperties.description);
		if(embedProperties.thumbnail.url) newEmbed.setThumbnail(embedProperties.thumbnail.url);
		if(embedProperties.author.name) newEmbed.setAuthor(embedProperties.author.name, embedProperties.author.icon_url, embedProperties.author.url);
		if(embedProperties.color) newEmbed.setColor(embedProperties.color);
		if(embedProperties.footer) newEmbed.setFooter(embedProperties.footer);
		if(embedProperties.image.url) newEmbed.setImage(embedProperties.image.url);
		if(embedProperties.timestamp) newEmbed.setTimestamp(embedProperties.timestamp);
		if(embedProperties.fields.length) {
			let i = 1;
			embedProperties.fields.forEach(async field => {
				const emoji = message.client.helpDeskEmojis[i];
				i++;
				field = `${emoji} ` + field;
				newEmbed.addField('\u200b', field, false);
				await hdMessage.react(emoji);
			});
		}
		if(helpDesk.specialQuestion && helpDesk.specialRole) {
			newEmbed.addField('\u200b', `❓ ${helpDesk.specialQuestion}`);
			await hdMessage.react('❓');
		}
		hdMessage.edit(newEmbed)
			.then(async msg=>{
				if(tempMessage) await tempMessage.delete().catch(() => {});
				message.client.replyEmbed.setDescription(`Embed correctly updated: [embed](${msg.url})`);
				await message.channel.send(message.client.replyEmbed);
			});
	},
};