const Discord = require('discord.js');

module.exports = {
	// Info
	name: 'new',
	description: 'Create a new #help-desk channel template.',
	aliases: ['newhd'],
	cooldown: 5,
	// Basic checks
	guildOnly: true,
	// Command Category
	helpdesk: true,
	// Permissions
	globalPerms: ['ADD_REACTIONS', 'SEND_MESSAGES', 'EMBED_LINKS', 'ATTACH_FILES', 'MANAGE_MESSAGES', 'READ_MESSAGE_HISTORY', 'VIEW_CHANNEL', 'MENTION_EVERYONE'],
	async execute(data, member, message) {
		await message.client.guildSchema.updateOne({ guildID: message.guild.id }, { $set:{ helpDesks: [] } });
		// Maximum amount of help desks reached
		if(data.helpDesks.length >= 5) {
			message.client.failureEmbed.setDescription('You already have 5 help desks. I can\'t handle more than that.\nIf you need to delete some simply delete their channel in Discord.');
			return message.channel.send(message.client.failureEmbed);
		}
		const hdChannel = await message.guild.channels.create('help-desk', {
			topic: 'Help Desk powered by the Official Help Desk Bot -> \'hd?help\' for info.',
			permissionOverwrites: [
				{
					id: message.guild.id,
					deny: ['ADD_REACTIONS', 'SEND_MESSAGES'],
				},
				{
					id: message.client.user.id,
					allow: ['ADD_REACTIONS', 'SEND_MESSAGES', 'EMBED_LINKS', 'ATTACH_FILES', 'MANAGE_MESSAGES', 'READ_MESSAGE_HISTORY', 'VIEW_CHANNEL', 'MENTION_EVERYONE'],
				},
			],
		});
		const hdEmbed = new Discord.MessageEmbed()
			.setTitle('[Title]')
			.setAuthor('[Author]', message.client.user.displayAvatarURL())
			.setThumbnail(message.client.user.displayAvatarURL())
			.setDescription('[Description]')
			.addFields(
				{ name: '\u200b', value: '1⃣ [Question example]' },
				{ name: '\u200b', value: '2⃣ [Question 2 example]' },
				{ name: '\u200b', value: '❓ [Special question which you can use to assign roles]' },
			)
			.setImage(message.client.user.displayAvatarURL())
			.setFooter('[Footer]')
			.setColor(message.guild.me.displayHexColor);

		const hdMessage = await hdChannel.send(hdEmbed);
		await hdMessage.react('1⃣');
		await hdMessage.react('2⃣');
		await hdMessage.react('❓');
		const helpDesk = {
			channelID: hdChannel.id,
			messageID: hdMessage.id,
			// Embed properties
			embedProperties: {
				title: '[Title]',
				description: '[Description]',
				url: undefined,
				timestamp: null,
				color: message.guild.me.displayColor,
				fields: [
					'[Question example]',
					'[Question 2 example]',
				],
				thumbnail: {
					url: message.client.user.displayAvatarURL(),
				},
				image: {
					url: message.client.user.displayAvatarURL(),
				},
				author: {
					name: '[Author]',
					url: undefined,
					icon_url: message.client.user.displayAvatarURL(),
				},
				footer: '[Footer]',
			},
			fieldsReplies: [
				'Simple answer',
				'Simple answer 2',
			],
			specialQuestion: '[Special Question]',
			specialTrigger: '?',
			specialRole: undefined,
		};

		// Update database
		data.helpDesks.push(helpDesk);
		await message.client.guildSchema.updateOne({ guildID: message.guild.id }, { $set:{ helpDesks: data.helpDesks } });
		// Update bot help desks cache
		await message.client.caches.hdel('settings', message.guild.id);
		// Reply to the command
		const replyEmbed = new Discord.MessageEmbed().setDescription(`I created a new <#${hdChannel.id}>.\nUse \`hd?tutorial\` to learn how to personalize it.`).setColor(message.client.mainColor);
		await message.channel.send(replyEmbed);
	},
};