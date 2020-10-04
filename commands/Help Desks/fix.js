module.exports = {
	// Info
	name: 'fix',
	description: 'If Help-Desk is not working correctly try running this command.',
	aliases: ['fixsettings'],
	cooldown: 5,
	// Basic checks
	guildOnly: true,
	// Command Category
	helpdesk: true,
	// Permissions
	async execute(data, member, message) {
		let issues = 0;
		await data.helpDesks.forEach(async hd => {
			const index = data.helpDesks.findIndex(desk => desk.messageID === hd.messageID);
			const hdChannel = await message.guild.channels.resolve(hd.channelID);
			if(!hdChannel) {
				data.helpDesks.splice(index, 1);
				issues++;
			}
			else {
				const requirePerms = ['ADD_REACTIONS', 'SEND_MESSAGES', 'EMBED_LINKS', 'ATTACH_FILES', 'MANAGE_MESSAGES', 'READ_MESSAGE_HISTORY', 'VIEW_CHANNEL', 'MENTION_EVERYONE'];
				if(!hdChannel.permissionsFor(message.guild.me.id).has(requirePerms)) {
					return message.channel.send(message.client.errorEmbed.setDescription(`I'm missing some important permissions in <#${hdChannel.id}>:\n**>** ${requirePerms.filter(permission => !hdChannel.permissionsFor(message.guild.me.id).has(permission)).join('\n**>** ')}`));
				}
				const hdMessage = await hdChannel.messages.fetch(hd.messageID);
				if(!hdMessage) {
					data.helpDesks.splice(index, 1);
					issues++;
				}
			}
		});
		if(issues > 0) {
			await message.client.guildSchema.updateOne({ guildID: message.guild.id }, { $set: { helpDesks: data.helpDesks } });
			await message.client.caches.hdel('settings', message.guild.id);

			await message.channel.send(message.client.replyEmbed.setDescription(`Fixed ${issues} issues.`));
		}
		else {
			await message.channel.send(message.client.replyEmbed.setDescription('No issues found.'));
		}
	},
};