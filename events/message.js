const Discord = require('discord.js');

module.exports = async (client, message) => {
	try {
		if (message.author.bot) return;

		let guildID, member, data;
		if (message.channel.type === 'text') {
			guildID = message.guild.id;
			member = message.member;
		}

		let prefix = 'hd?';
		let args = [];
		let deskIndex = 0;

		// Check if bot is mentioned
		let botMentioned = false;
		if (message.mentions.users.size) {
			if (message.mentions.users.first().id === client.user.id) {
				botMentioned = true;
			}
		}
		if (!message.content.startsWith(prefix) && !message.content.startsWith('<@!739796627681837067>')) return;

		// Get the args of the message
		if (message.content.startsWith('<@!739796627681837067>')) {
			args = message.content.slice(23).split(/ +/);
		}
		else {
			args = message.content.slice(prefix.length).split(/ +/);
		}

		// Check if it's a command
		const commandName = args.shift().toLowerCase();
		const command = client.commands.get(commandName)
			|| client.commands.find(cmd => cmd.aliases && cmd.aliases.includes(commandName));
		if (!command) return;

		// Check cooldowns first
		// Cooldowns
		const cooldowns = client.cooldowns;
		if (!cooldowns.has(command.name)) cooldowns.set(command.name, new Discord.Collection());
		const now = Date.now();
		const timestamps = cooldowns.get(command.name);
		const cooldownAmount = (command.cooldown || 3) * 1000;
		if (timestamps.has(message.author.id)) {
			const expirationTime = timestamps.get(message.author.id) + cooldownAmount;

			if (now < expirationTime) {
				const timeLeft = (expirationTime - now) / 1000;
				client.failureEmbed.setTitle('\\🛠️  **You are being rate limited**')
					.setDescription(`*<@${message.author.id}> please wait ${timeLeft.toFixed(1)} more second(s) before reusing the \`${command.name}\` command.*`);
				await message.author.send(client.failureEmbed).catch();
				return client.failureEmbed.setTitle('\\❗  **Help Desk Failure** \\❗');
			}
		}
		timestamps.set(message.author.id, now);
		setTimeout(() => timestamps.delete(message.author.id), cooldownAmount);

		// Get data from the database if not already done
		if(!data && guildID) {
			data = await client.guildSchema.findOne({guildID: message.guild.id});
			if(!data) {
				data = {
					guildID: guildID,
					helpDesks: [],
				}
				// Create the new guild object for the database
				const newGuild = new client.guildSchema(data);
				// Save the object in the database
				await newGuild.save().catch(err => console.log(err));
			}
		}
		// Check if command is from DM and if it can be executed
		if (command.guildOnly && message.channel.type !== 'text') {
			client.failureEmbed.setDescription('*I\'m unable to execute that command inside DMs.*');
			return message.channel.send(client.failureEmbed);
		}

		// Bot permissions needed
		// Send Messages
		if (guildID && !message.channel.permissionsFor(message.guild.me.id).has('SEND_MESSAGES')) {
			client.failureEmbed.setDescription(`I don\'t have SEND_MESSAGES permission in *${member.guild.name}* server. Please report this to the server moderators.`);
			message.author.send(client.failureEmbed).catch();
			return;
		}
		// Other permissions
		if (guildID && command.perms && !message.channel.permissionsFor(message.guild.me.id).has(command.perms)) {
			client.failureEmbed.setDescription(`I'm missing some essential permissions to run this command.\nMake sure to grant them in this **channel settings**:\n**>** ${command.perms.filter(permission => !message.channel.permissionsFor(message.guild.me.id).has(permission)).join('\n**>** ')}`);
			await message.channel.send(client.failureEmbed);
			return;
		}
		if (guildID && command.globalPerms && !command.globalPerms.every(permission => message.guild.me.permissions.has(permission))) {
			client.failureEmbed.setDescription(`I'm missing some important permissions to run this commands.\nMake sure to grant them in **server settings:**\n**>** ${command.globalPerms.filter(permission => !message.guild.me.permissions.has(permission)).join('\n**>** ')}`);
			await message.channel.send(client.failureEmbed);
			return;
		}

		// Check if member has commander role or has permissions to use commands
		const isModerator = guildID ? member.hasPermission('MANAGE_MESSAGES') : false;

		// Check if command requires args
		if (command.args) {
			let regexp = command.args;
			const matched = args.join(' ').match(regexp);
			if(!matched) {
				client.errorEmbed.setDescription('*Incorrect usage of the command!*' +
					`\nThe proper usage would be: \`${prefix}${command.name} ${command.usage}\``);
				client.errorEmbed.setFooter(`Type '${prefix}help ${commandName}' for more info`)
				await message.channel.send(client.errorEmbed);
				return client.errorEmbed.setFooter('For support use <>help');
			}
			args = matched.slice(1);
		}

		if ((command.helpdesk || command.embed) && !isModerator) {
			client.errorEmbed.setDescription(`*<@${message.author.id}> you do not have the required permissions to use this command (Manage Messages).*`);
			return message.channel.send(client.errorEmbed);
		}

		// Developer command
		if (command.dev) {
			// Run the command only if it was used by a developer
			if (message.author.id === '256851887502917633') {
				command.execute(message, args);
			}
			return;
		}
		// Commands which require choosing a generator
		if (command.chooseDesk) {
			let desks = data.helpDesks;
			// If there are no generators return
			if (desks.length === 0) {
				client.errorEmbed.setDescription(`*<@${message.author.id}> I haven't found any #help-desk in this server!*\nUse the \`<>tutorial\` command to learn how to create one.`);
				return message.channel.send(client.errorEmbed);
			}
			if (desks.length !== 1) {
				let choiceEmbed = new Discord.MessageEmbed()
					.setTitle('#Help-Desk Picker')
					.setColor(client.mainColor);
				choiceEmbed.setDescription(`*<@${message.author.id}> choose one of this Help Desks:*`);
				for (let i = 0; i < desks.length; i++) {
					const desk = message.guild.channels.resolve(desks[i].channelID);
					if (desk) {
						choiceEmbed.addField(`Help Desk #${i + 1}`, `<#${desk.id}>`);
					}
				}
				choiceEmbed.addField('How to choose?', 'To choose one of these Help Desks just type it\'s number in chat\nFor example, for the first Help Desk (#1) type 1');
				// Wait the reply from who used the command
				message.channel.send(choiceEmbed)
					.then(msg => {
						const filter = (m) => m.author.id === message.author.id;
						const collector = msg.channel.createMessageCollector(filter, { time: 30000 });

						collector.on('collect', m => {
							if (isNaN(m.content) || m.content > desks.length) {
								msg.channel.send('Invalid number, try again.');
							}
							else {
								deskIndex = m.content - 1;
								msg.delete();
								m.delete();
								collector.stop();
							}
						});
						collector.on('end', async () => {
							// Execute the command
							try {
								await command.execute(data, member, message, args, deskIndex);
							}
							catch (err) {
								await client.errorReport(err, message, message.channel);
							}
						});
					});
			}
			else {
				// Execute the command
				try {
					await command.execute(data, member, message, args, deskIndex);
				}
				catch (err) {
					await client.errorReport(err, message, message.channel);
				}
			}
		}
		// Standard Commands
		else {
			// Execute the command
			try {
				await command.execute(data, member, message, args, deskIndex);
			}
			catch (err) {
				await client.errorReport(err, message, message.channel);
			}
		}
	}
	catch (err) {
		await client.errorReport(err, message, message.channel);
	}
};