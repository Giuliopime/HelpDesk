const Discord = require('discord.js');

module.exports = async (client, message) => {
	try {
		// Don't listen to bots or non-guild messages
		if (message.author.bot || !message.guild) return;

		// Define some useful variables (you can change the prefix here)
		const guildID = message.guild.id, member = message.member;
		let prefix = 'hd?', args = [], deskIndex = 0;

		// Check if the message starts with the prefix
		if (!message.content.startsWith(prefix) && !message.content.startsWith(`<@!${client.user.id}>` || `<@${client.user.id}>`)) return;


		// Check if the message is a command and get the args
		if (message.content.startsWith(`<@!${client.user.id}>` || `<@${client.user.id}>`))
			args = message.content.slice(23).split(/ +/);
		else
			args = message.content.slice(prefix.length).split(/ +/);

		const commandName = args.shift().toLowerCase();
		const command = client.commands.get(commandName)
			|| client.commands.find(cmd => cmd.aliases && cmd.aliases.includes(commandName));
		if (!command) return;


		// Cooldowns
		// Check user Global Cooldown
		const isOnCooldown = await client.checkGCD(member.id);
		if(isOnCooldown) return;

		// Command cooldowns
		// Cooldowns are saved in the redis cache, with an hash called 'cooldowns' which contains a list of commands identified by their name, each of which contains timestamps of the users who are on a cooldown (userID + timestamp)
		// You cannot save objects inside redis caches, so whenever we want to save an object in our cooldown hash we need to convert it to a string (I'm using JSON.stringify())
		// For these reason when we get the value from the hash we need to convert it to an object with the JSON.parse() method
		let timestamps = await client.caches.hget('cooldowns', command.name);
		if (!timestamps) timestamps = '{}';
		timestamps = JSON.parse(timestamps);

		// This cooldown-checking process is inspired to the one you can find on the discord.js guide (https://discordjs.guide/command-handling/adding-features.html#cooldowns)
		const now = Date.now();
		const cooldownAmount = (command.cooldown || 2) * 1000;
		if (timestamps[member.id]) {
			const expirationTime = timestamps[member.id] + cooldownAmount;

			if (now < expirationTime) {
				const timeLeft = (expirationTime - now) / 1000;
				client.failureEmbed.setTitle('\\🛠️  **You are being rate limited**')
					.setDescription(`*<@${message.author.id}> please wait ${timeLeft.toFixed(1)} more second(s) before reusing the \`${command.name}\` command.*`);
				await message.author.send(client.failureEmbed).catch(() => {});
				client.failureEmbed.setTitle('\\❗  **Help Desk Failure** \\❗');
				return
			}
		}
		timestamps[member.id] = now;
		await client.caches.hset('cooldowns', command.name, JSON.stringify(timestamps));
		setTimeout(async () => {
			delete timestamps[member.id];
			await client.caches.hset('cooldowns', command.name, JSON.stringify(timestamps));
		}, cooldownAmount);



		// Same thing for the cooldowns, we get the guild settings from the hash called 'settings' of our redis cache
		// If the cache doesn't have the guild settings stored then we fetch them from the database
		// Once we fetch them we update our redis cache including the new guild settings we got from the database
		// Our settings hash looks like this: settings (which is the name) --> array of guild IDs --> each of which contains the corresponding guild settings (see guildS.js in /utils)
		let data = await client.caches.hget('settings', guildID);
		if (!data) {
			data = await client.guildSchema.findOne({ guildID: guildID });
			if(!data) {
				data = {
					guildID: guildID,
					helpDesks: [],
				}
				const newGuild = new client.guildSchema(data);
				// Save the object in the database
				await newGuild.save().catch(err => console.log(err));
			}
			await client.caches.hset('settings', guildID, JSON.stringify(data));
		}
		else data = JSON.parse(data);
		console.log(data)


		// Send Messages Permission
		if (!message.channel.permissionsFor(message.guild.me.id).has('SEND_MESSAGES')) {
			client.failureEmbed.setDescription(`I don\'t have SEND_MESSAGES permission in *${member.guild.name}* server. Please report this to the server moderators.`);
			message.author.send(client.failureEmbed).catch(() => {});
			return;
		}
		// Check the bot permissions required for each command
		if (command.perms && !message.channel.permissionsFor(message.guild.me.id).has(command.perms)) {
			client.failureEmbed.setDescription(`I'm missing some essential permissions to run this command.\nMake sure to grant them in this **channel settings**:\n**>** ${command.perms.filter(permission => !message.channel.permissionsFor(message.guild.me.id).has(permission)).join('\n**>** ')}`);
			await message.channel.send(client.failureEmbed);
			return;
		}
		if (command.globalPerms && !message.guild.me.permissions.has(command.globalPerms)) {
			client.failureEmbed.setDescription(`I'm missing some important permissions to run this commands.\nMake sure to grant them in **server settings:**\n**>** ${command.globalPerms.filter(permission => !message.guild.me.permissions.has(permission)).join('\n**>** ')}`);
			await message.channel.send(client.failureEmbed);
			return;
		}


		// Check if command requires args (matching the command RegExp property called args)
		if (command.args) {
			const regexp = command.args;
			const matched = args.join(' ').match(regexp);
			if(!matched) {
				client.errorEmbed.setDescription('*Incorrect usage of the command!*' +
					`\nThe proper usage would be: \`${prefix}${command.name} ${command.usage}\``);
				client.errorEmbed.setFooter(`Type '${prefix}help ${commandName}' for more info`)
				await message.channel.send(client.errorEmbed);
				client.errorEmbed.setFooter('For support use <>help');
				return;
			}

			args = matched.slice(1);
		}

		// Check if user has permissions to use management commands
		if ((command.helpdesk || command.embed) && !member.hasPermission('MANAGE_MESSAGES')) {
			client.errorEmbed.setDescription(`*<@${message.author.id}> you do not have the required permissions to use this command (Manage Messages).*`);
			await message.channel.send(client.errorEmbed);
			return;
		}

		// Developer commands
		if (command.dev) {
			// Run the command only if it was used by a developer
			if (message.author.id === '256851887502917633') await command.execute(message, args);
			return;
		}

		// Commands which require to choose an #help-desk
		if (command.chooseDesk) {
			const desks = data.helpDesks;
			// If there are no help desks in the server report it and return
			if (desks.length === 0) {
				client.errorEmbed.setDescription(`*<@${message.author.id}> I haven't found any #help-desk in this server!*\nUse the \`<>tutorial\` command to learn how to create one.`);
				await message.channel.send(client.errorEmbed);
				return;
			}
			// If the help desks are more than 1 then the user needs to choose one
			if (desks.length !== 1) {
				// Create the choice embed message
				const choiceEmbed = new Discord.MessageEmbed()
					.setTitle('#Help-Desk Picker')
					.setColor(client.mainColor)
					.setDescription(`*<@${message.author.id}> choose one of this Help Desks:*`);
				// List the Help Desks
				for (let i = 0; i < desks.length; i++) {
					const desk = await message.guild.channels.resolve(desks[i].channelID);
					if (desk) choiceEmbed.addField(`Help Desk #${i + 1}`, `<#${desk.id}>`);
				}
				choiceEmbed.addField('How to choose?', 'To choose one of these Help Desks just type it\'s number in chat\nFor example, for the first Help Desk (#1) type 1');

				// Wait for a reply
				message.channel.send(choiceEmbed)
					.then(msg => {
						// Allow only the user who used to command to reply
						const filter = (m) => m.author.id === message.author.id;
						const collector = msg.channel.createMessageCollector(filter, { time: 30000 });

						collector.on('collect', async m => {
							// The reply is not an help desk number...
							if (isNaN(m.content) || m.content > desks.length) msg.channel.send('Invalid number, try again.');
							else {
								deskIndex = m.content - 1;
								await msg.delete();
								await m.delete();
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
			// If instead there is only one help desk in the server...
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

		// Standard Commands which don't require choosing an Help Desk
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